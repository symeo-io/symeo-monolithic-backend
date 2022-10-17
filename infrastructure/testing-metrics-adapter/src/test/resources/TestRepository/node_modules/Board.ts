import { embed } from '@aws/dynamodb-data-mapper';
import { attribute, hashKey, table } from '@aws/dynamodb-data-mapper-annotations';
import * as cuid from 'cuid';
import * as moment from 'moment';
import AbstractEntity from '../AbstractEntity';
import User from '../user/User';
import EpicTag from './EpicTag';
import Milestone from './Milestone';
import Sprint from './Sprint';
import SprintDurationType from './SprintDurationType';
import Zone from './Zone';
import ZoneType from './ZoneType';
import Dependency from './Dependency';

const DEFAULT_SPRINT_NUMBER = 4;
const FIRST_EPIC_LABEL = 'This is a 20-point EPIC, you can click on it to modify it';
const SECOND_EPIC_LABEL = '10-point EPIC';
const THIRD_EPIC_LABEL = '15-point EPIC';
const FOURTH_EPIC_LABEL = '30 points EPIC';
const FIRST_DEPENDENCY_LABEL = 'This is a dependency';
const SECOND_DEPENDENCY_LABEL = 'This is another dependency';
const THIRD_DEPENDENCY_LABEL = 'Dependency';
const FOURTH_DEPENDENCY_LABEL = 'Dependency';
const FIRST_DEPENDENCY_ESTIMATION = 0;
const SECOND_DEPENDENCY_ESTIMATION = 2;
const THIRD_DEPENDENCY_ESTIMATION = 2.5;
const FOURTH_DEPENDENCY_ESTIMATION = 10;
const MILESTONE_LABEL = 'This is a milestone';
const TAG_LABEL = 'Purple tag name here';
const TAG_COLOR = 'purple';

@table(process.env.DYNAMO_BOARDS_TABLE_NAME)
export default class Board extends AbstractEntity {
    @hashKey({ attributeName: '_id' })
    public id: string;

    @attribute({ attributeName: '_name' })
    public name: string;

    @attribute({ attributeName: '_slug' })
    public slug: string;

    @attribute({ attributeName: '_slugId' })
    public slugId: string;

    @attribute({
        attributeName: '_startDate',
        defaultProvider: /* istanbul ignore next */ () => new Date(),
    })
    public startDate: Date;

    @attribute({ attributeName: '_firstSprintNumber' })
    public firstSprintNumber: number;

    @attribute({ attributeName: '_sprintDurationType' })
    public sprintDurationType: string;

    @attribute({ attributeName: '_sprintDuration' })
    public sprintDuration: number;

    @attribute({ attributeName: '_sprints', memberType: embed(Sprint) })
    public sprints: Sprint[];

    @attribute({ attributeName: '_milestones', memberType: embed(Milestone) })
    public milestones: Milestone[];

    @attribute({ attributeName: '_members' })
    public members: string[];

    @attribute({ attributeName: '_epicTags', memberType: embed(EpicTag) })
    public epicTags: EpicTag[];

    @attribute({ attributeName: '_zones', memberType: embed(Zone) })
    public zones: Zone[];

    @attribute({ attributeName: '_isPublic' })
    public isPublic: boolean;

    constructor(
        id?: string,
        name?: string,
        slug?: string,
        slugId?: string,
        startDate?: Date,
        firstSprintNumber?: number,
        sprintDurationType?: string,
        sprintDuration?: number,
        sprints?: Sprint[],
        milestones?: Milestone[],
        members?: string[],
        epicTags?: EpicTag[],
        zones?: Zone[],
        isPublic?: boolean,
        createdAt?: Date,
    ) {
        super(createdAt);
        this.id = id || cuid();
        this.name = name;
        this.slug = slug || Board.generateSlug(this.name);
        this.slugId = slugId || cuid.slug();
        this.startDate = startDate || new Date();
        this.firstSprintNumber = firstSprintNumber || 1;
        this.sprintDuration = sprintDuration || 1;
        this.sprintDurationType = sprintDurationType || SprintDurationType.Week;
        this.sprints = sprints || [];
        this.milestones = milestones || [];
        this.members = members || [];
        this.epicTags = epicTags || [];
        this.zones = zones || Board.generateDefaultZones();
        this.isPublic = isPublic || false;
    }

    get currentSprintIndex() {
        const now = moment();
        const boardStartDate = moment(this.startDate);

        if (boardStartDate.isAfter(now)) {
            return -1;
        }

        const daysDiff = now.diff(boardStartDate, 'days');

        return this.sprintDurationType === SprintDurationType.Day
            ? daysDiff
            : Math.floor(daysDiff / (this.sprintDuration * 7));
    }

    public addZone(zone: Zone): void {
        this.zones.push(zone);

        if (zone.type === ZoneType.EPIC) {
            this.sprints.forEach(sprint => sprint.addZoneEpics(zone.id));
        }

        if (zone.type === ZoneType.DEPENDENCY) {
            this.sprints.forEach(sprint => sprint.addZoneDependencies(zone.id));
        }
    }

    public deleteZone(zoneId: string): void {
        const zoneIndex = this.zones.findIndex(zone => zone.id === zoneId);

        if (zoneIndex === -1) {
            throw new Error(`Zone '${zoneId}' does not exist on board`);
        }

        const zoneType = this.zones[zoneIndex].type;

        if (zoneType === ZoneType.DEPENDENCY) {
            // Only dependency zones can be deleted for now
            this.sprints.forEach(sprint => {
                const sprintDependencies = sprint.getZoneDependenciesByZoneId(zoneId);
                /* istanbul ignore else  */
                if (sprintDependencies) {
                    // remove the link from epics to the dependencies in the deleted zone
                    sprintDependencies.dependencies.forEach(dependency => {
                        this.sprints.forEach(secondSprint => {
                            this.deleteDependency(zoneId, secondSprint.id, dependency.id);
                        });
                    });
                }
            });

            this.sprints.forEach(sprint => {
                // remove the deleted zone to the sprint
                sprint.deleteDependencyZone(zoneId);
            });
            // remove the zone
            this.zones = this.zones.filter(zone => zone.id !== zoneId);
        }
    }

    public reorderZone(sourceIndex: number, destinationIndex: number): void {
        const result = Array.from(this.zones);
        const [removed] = result.splice(sourceIndex, 1);
        result.splice(destinationIndex, 0, removed);

        this.zones = result;
    }

    public addSprint(sprintIndex?: number): void {
        const newSprint = new Sprint();
        this.zones.forEach(zone => {
            if (zone.type === ZoneType.EPIC) {
                newSprint.addZoneEpics(zone.id);
            }

            if (zone.type === ZoneType.DEPENDENCY) {
                newSprint.addZoneDependencies(zone.id);
            }
        });

        if (sprintIndex === null || sprintIndex === undefined) {
            this.sprints.push(newSprint);
        } else {
            this.sprints.splice(sprintIndex, 0, newSprint);

            // Move dependency dueDate to match new sprint
            this.sprints.forEach((sprint, index) => {
                if (index > sprintIndex) {
                    sprint.zoneDependenciesList.forEach(zoneDependency => {
                        zoneDependency.dependencies.forEach(dependency => {
                            if (this.sprintDurationType === SprintDurationType.Day) {
                                dependency.dueDate = moment(dependency.dueDate)
                                    .add(1, 'days')
                                    .toDate();
                            } else {
                                dependency.dueDate = moment(dependency.dueDate)
                                    .add(this.sprintDuration, 'weeks')
                                    .toDate();
                            }
                        });
                    });
                }
            });
        }
    }

    public removeSprint(sprintId: string): void {
        /* istanbul ignore else  */
        if (this.sprints.length > 1) {
            const sprintIndex = this.sprints.findIndex(sprint => sprint.id === sprintId);
            const [sprintRemoved] = this.sprints.splice(sprintIndex, 1);

            const dependenciesDeleted = sprintRemoved.zoneDependenciesList.reduce((accumulator, zone) => {
                return [...accumulator, ...zone.dependencies.map(dependency => dependency.id)];
            }, []);

            this.sprints.forEach(sprint => {
                sprint.zoneEpicsList.forEach(epicList => {
                    epicList.epics.forEach(epic => {
                        epic.deleteDependencies(dependenciesDeleted);
                    });
                });
            });

            this.sprints.forEach(sprint => {
                sprint.zoneDependenciesList.forEach(dependencyList => {
                    dependencyList.dependencies.forEach(dependency => {
                        dependency.deleteDependencies(dependenciesDeleted);
                    });
                });
            });

            // Move dependency dueDate to match new sprint
            this.sprints.forEach((sprint, index) => {
                if (index >= sprintIndex) {
                    sprint.zoneDependenciesList.forEach(zoneDependency => {
                        zoneDependency.dependencies.forEach(dependency => {
                            if (this.sprintDurationType === SprintDurationType.Day) {
                                dependency.dueDate = moment(dependency.dueDate)
                                    .subtract(1, 'days')
                                    .toDate();
                            } else {
                                dependency.dueDate = moment(dependency.dueDate)
                                    .subtract(this.sprintDuration, 'weeks')
                                    .toDate();
                            }
                        });
                    });
                }
            });
        }
    }

    public moveEpic(
        zoneId: string,
        sourceSprintId: string,
        destinationSprintId: string,
        sourceIndex: number,
        destinationIndex: number,
    ): void {
        const epicZone = this.zones.find(zone => zone.id === zoneId);
        if (!epicZone || epicZone.type !== ZoneType.EPIC) {
            throw new Error(`Zone '${zoneId}' does not exist for EPICs on board`);
        }

        const sourceSprintIndex = this.sprints.findIndex(sprint => sprint.id === sourceSprintId);
        if (sourceSprintIndex === -1) {
            throw new Error(`Sprint '${sourceSprintId}' does not exist on board`);
        }

        if (sourceSprintId === destinationSprintId) {
            return this.sprints[sourceSprintIndex].reorderEpic(zoneId, sourceIndex, destinationIndex);
        }

        const destinationSprintIndex = this.sprints.findIndex(
            sprint => sprint.id === destinationSprintId,
        );
        if (destinationSprintIndex === -1) {
            throw new Error(`Sprint '${destinationSprintIndex}' does not exist on board`);
        }

        const sourceClone = Array.from(
            this.sprints[sourceSprintIndex].getZoneEpicsByZoneId(zoneId).epics,
        );
        const destinationClone = Array.from(
            this.sprints[destinationSprintIndex].getZoneEpicsByZoneId(zoneId).epics,
        );
        const [removed] = sourceClone.splice(sourceIndex, 1);

        destinationClone.splice(destinationIndex, 0, removed);

        this.sprints[sourceSprintIndex].getZoneEpicsByZoneId(zoneId).epics = sourceClone;
        this.sprints[destinationSprintIndex].getZoneEpicsByZoneId(zoneId).epics = destinationClone;
    }

    public moveDependency(
        sourceZoneId: string,
        destinationZoneId: string,
        sourceSprintId: string,
        destinationSprintId: string,
        sourceIndex: number,
        destinationIndex: number,
    ): void {
        const dependencySourceZone = this.zones.find(zone => zone.id === sourceZoneId);
        if (!dependencySourceZone || dependencySourceZone.type !== ZoneType.DEPENDENCY) {
            throw new Error(`Zone '${sourceZoneId}' does not exist for Dependencies on board`);
        }

        const dependencyDestinationZone = this.zones.find(zone => zone.id === destinationZoneId);
        if (!dependencyDestinationZone || dependencyDestinationZone.type !== ZoneType.DEPENDENCY) {
            throw new Error(`Zone '${destinationZoneId}' does not exist for Dependencies on board`);
        }

        const sourceSprintIndex = this.sprints.findIndex(sprint => sprint.id === sourceSprintId);
        if (sourceSprintIndex === -1) {
            throw new Error(`Sprint '${sourceSprintId}' does not exist on board`);
        }

        if (sourceSprintId === destinationSprintId && sourceZoneId === destinationZoneId) {
            return this.sprints[sourceSprintIndex].reorderDependency(
                sourceZoneId,
                sourceIndex,
                destinationIndex,
            );
        }

        const destinationSprintIndex = this.sprints.findIndex(
            sprint => sprint.id === destinationSprintId,
        );
        if (destinationSprintIndex === -1) {
            throw new Error(`Sprint '${destinationSprintIndex}' does not exist on board`);
        }

        const sourceClone = Array.from(
            this.sprints[sourceSprintIndex].getZoneDependenciesByZoneId(sourceZoneId).dependencies,
        );

        const destinationClone = Array.from(
            this.sprints[destinationSprintIndex].getZoneDependenciesByZoneId(destinationZoneId)
                .dependencies,
        );
        const [removed] = sourceClone.splice(sourceIndex, 1) as Dependency[];

        // Move dependency dueDate to match new sprint
        if (this.sprintDurationType === SprintDurationType.Day) {
            removed.dueDate = moment(removed.dueDate)
                .add(destinationSprintIndex - sourceSprintIndex, 'days')
                .toDate();
        } else {
            removed.dueDate = moment(removed.dueDate)
                .add((destinationSprintIndex - sourceSprintIndex) * this.sprintDuration, 'weeks')
                .toDate();
        }

        // Check all dependencies and check if its parents are still after
        if (destinationSprintIndex > sourceSprintIndex) {
            this.sprints.forEach(sprint => {
                sprint.zoneDependenciesList.forEach(dependencyList => {
                    dependencyList.dependencies.forEach(dependency => {
                        if (dependency.dependencies.includes(removed.id)) {
                            if (moment(dependency.dueDate) < moment(removed.dueDate)) {
                                dependency.dependencies = dependency.dependencies.filter(id => removed.id !== id);
                                removed.dependencies.push(dependency.id);
                            }
                        }
                    });
                });
            });
        } else {
            this.sprints.forEach(sprint => {
                sprint.zoneDependenciesList.forEach(dependencyList => {
                    dependencyList.dependencies.forEach(dependency => {
                        if (removed.dependencies.includes(dependency.id)) {
                            if (moment(dependency.dueDate) > moment(removed.dueDate)) {
                                removed.dependencies = removed.dependencies.filter(id => id !== dependency.id);
                                dependency.dependencies.push(removed.id);
                            }
                        }
                    });
                });
            });
        }

        destinationClone.splice(destinationIndex, 0, removed);

        this.sprints[sourceSprintIndex].getZoneDependenciesByZoneId(
            sourceZoneId,
        ).dependencies = sourceClone;
        this.sprints[destinationSprintIndex].getZoneDependenciesByZoneId(
            destinationZoneId,
        ).dependencies = destinationClone;
    }

    public updateSprintDependencyLinksOnEdit(
        sprintId: string,
        zoneId: string,
        dependencyEditedId: string,
    ): void {
        const dependencySprintIndex = this.sprints.findIndex(sprint => sprint.id === sprintId);
        if (dependencySprintIndex === -1) {
            throw new Error(`Sprint '${sprintId}' does not exist on board`);
        }

        const zoneIndex = this.sprints[dependencySprintIndex].zoneDependenciesList.findIndex(
            zone => zone.zoneId === zoneId,
        );
        if (zoneIndex === -1) {
            throw new Error(`Zone '${zoneId}' does not exist on sprint ${sprintId}`);
        }

        const dependencyEdited = this.sprints[dependencySprintIndex].zoneDependenciesList[
            zoneIndex
            ].dependencies.find(dependency => dependency.id === dependencyEditedId);
        if (!dependencyEdited) {
            throw new Error(`Dependency '${dependencyEditedId}' does not exist in zone ${zoneId}`);
        }

        this.sprints[dependencySprintIndex].zoneDependenciesList.forEach(zoneDependency => {
            zoneDependency.dependencies.forEach(dependency => {
                if (dependency.dependencies.includes(dependencyEdited.id)) {
                    if (moment(dependency.dueDate) < moment(dependencyEdited.dueDate)) {
                        dependency.dependencies = dependency.dependencies.filter(
                            id => id !== dependencyEdited.id,
                        );
                        dependencyEdited.dependencies.push(dependency.id);
                    }
                }
                if (dependencyEdited.dependencies.includes(dependency.id)) {
                    if (moment(dependency.dueDate) > moment(dependencyEdited.dueDate)) {
                        dependencyEdited.dependencies = dependencyEdited.dependencies.filter(
                            id => id !== dependency.id,
                        );
                        dependency.dependencies.push(dependencyEdited.id);
                    }
                }
            });
        });
    }

    public addMember(member: User): void {
        if (!this.members.includes(member.email)) {
            this.members.push(member.email);
        }
    }

    public removeMember(member: User): void {
        this.members = this.members.filter(email => email !== member.email);
        this.sprints.forEach(sprint => sprint.removeUserAssignation(member.email));
    }

    public deleteDependency(zoneId: string, sprintId: string, dependencyId: string) {
        const zoneIndex = this.zones.findIndex(zone => zone.id === zoneId);
        if (zoneIndex === -1) {
            throw new Error(`Zone '${zoneId}' does not exist on board`);
        }

        const dependencySprint = this.sprints.find(sprint => sprint.id === sprintId);
        if (!dependencySprint) {
            throw new Error(`Sprint '${sprintId}' does not exist on board`);
        }

        dependencySprint.deleteDependency(zoneId, dependencyId);
        this.sprints.forEach(sprint =>
            sprint.zoneEpicsList.forEach(zoneEpics => zoneEpics.deleteDependency(dependencyId)),
        );
        this.sprints.forEach(sprint =>
            sprint.zoneDependenciesList.forEach(zoneDependencies =>
                zoneDependencies.deleteDependencyLink(dependencyId),
            ),
        );
    }

    public addEpicTag(name: string, color: string): void {
        this.epicTags.push(new EpicTag(null, name, color));
    }

    public updateEpicTag(id: string, name: string, color: string): void {
        const epicTag = this.epicTags.find(tag => tag.id === id);
        epicTag.name = name;
        epicTag.color = color;
    }

    public deleteEpicTag(id: string): void {
        this.epicTags = this.epicTags.filter(epicTag => epicTag.id !== id);
        this.sprints.forEach(sprint => sprint.removeEpicTag(id));
    }

    public createMilestone(name: string, date: Date) {
        this.milestones.push(new Milestone(null, name, date));
    }

    public deleteMilestone(id: string): void {
        this.milestones = this.milestones.filter(milestone => milestone.id !== id);
    }

    public static translateId(idDic: any): (id: string) => string {
        return (id: string) => {
            if (!id) {
                return null;
            }
            if (idDic[id] === undefined) {
                idDic[id] = cuid();
            }
            return idDic[id];
        };
    }
    public duplicate(currentUserEmail: string, name: string, keepMembers: boolean): Board {
        // warning, for duplicate to work, the zoneId present in sprints' zoneEpics
        // and zoneDependencies must be in sync with the ids of duplicated zones
        const newIds = {};
        const translation = Board.translateId(newIds);
        const members = keepMembers ? this.members : [currentUserEmail];

        return new Board(
            null,
            name,
            null,
            null,
            this.startDate,
            this.firstSprintNumber,
            this.sprintDurationType,
            this.sprintDuration,
            this.sprints.map(sprint => sprint.duplicate(translation, keepMembers)),
            this.milestones.map(milestone => milestone.duplicate()),
            members,
            this.epicTags.map(epicTag => epicTag.duplicate(translation)),
            this.zones.map(zone => zone.duplicate(translation(zone.id), keepMembers)),
            this.isPublic,
            this.createdAt,
        );
    }

    public applyStaffingToFollowingSprints(sprintIndex: number): void {
        if (sprintIndex > this.sprints.length - 1) {
            return;
        }

        const assignees = this.sprints[sprintIndex].assignees;

        for (let i = sprintIndex + 1; i < this.sprints.length; i++) {
            this.sprints[i].assignees = assignees;
        }
    }

    public fillBoardWithDefaultData() {
        // TODO: unit test
        const milestoneDay = new Date(this.startDate);
        if (this.sprintDurationType === SprintDurationType.Day) {
            milestoneDay.setDate(milestoneDay.getDate() + 3);
        } else {
            milestoneDay.setDate(milestoneDay.getDate() + 3 * 7 * this.sprintDuration - 1);
        }
        this.createMilestone(MILESTONE_LABEL, milestoneDay);

        this.addEpicTag(TAG_LABEL, TAG_COLOR);

        const tagId = this.epicTags[0].id;

        for (let sprintIndex = 0; sprintIndex < DEFAULT_SPRINT_NUMBER; sprintIndex++) {
            // TODO: put un board core model
            const epicZoneId = this.zones.filter(zone => zone.type === ZoneType.EPIC)[0].id;
            const firstDependencyZoneId = this.zones.filter(zone => zone.type === ZoneType.DEPENDENCY)[0]
                .id;
            const secondDependencyZoneId = this.zones.filter(zone => zone.type === ZoneType.DEPENDENCY)[1]
                .id;

            this.addSprint();
            this.sprints[sprintIndex].capacity = 30;

            const firstDayOfNextSprint = new Date(this.startDate);
            if (this.sprintDurationType === SprintDurationType.Day) {
                firstDayOfNextSprint.setDate(firstDayOfNextSprint.getDate() + sprintIndex);
            } else {
                firstDayOfNextSprint.setDate(
                    firstDayOfNextSprint.getDate() + sprintIndex * 7 * this.sprintDuration,
                );
            }
            switch (sprintIndex) {
                case 0:
                    this.sprints[0].addDependency(
                        firstDependencyZoneId,
                        FIRST_DEPENDENCY_LABEL,
                        new Date(this.startDate),
                        false,
                        '',
                        FIRST_DEPENDENCY_ESTIMATION,
                    );
                    break;
                case 1:
                    this.sprints[1].addEpic(epicZoneId, FIRST_EPIC_LABEL, 20, false, false, tagId);

                    this.sprints[1].addDependency(
                        firstDependencyZoneId,
                        SECOND_DEPENDENCY_LABEL,
                        firstDayOfNextSprint,
                        false,
                        '',
                        SECOND_DEPENDENCY_ESTIMATION,
                    );

                    this.sprints[1].addDependency(
                        secondDependencyZoneId,
                        THIRD_DEPENDENCY_LABEL,
                        firstDayOfNextSprint,
                        false,
                        '',
                        THIRD_DEPENDENCY_ESTIMATION,
                    );
                    break;
                case 2:
                    this.sprints[2].addEpic(epicZoneId, SECOND_EPIC_LABEL, 10, false, false, tagId);
                    this.sprints[2].addEpic(epicZoneId, THIRD_EPIC_LABEL, 15, false, false);

                    this.sprints[2].addDependency(
                        secondDependencyZoneId,
                        FOURTH_DEPENDENCY_LABEL,
                        firstDayOfNextSprint,
                        false,
                        '',
                        FOURTH_DEPENDENCY_ESTIMATION,
                    );
                    break;
                case 3:
                    this.sprints[3].addEpic(epicZoneId, FOURTH_EPIC_LABEL, 30, false, false);
                    break;
            }
        }
    }

    public static generateSlug(name: string): string {
        if (!name) {
            return null;
        }

        return name
            .trim()
            .toLowerCase()
            .replace(/ /g, '-');
    }

    private static generateDefaultZones(): Zone[] {
        return [
            new Zone(null, ZoneType.GANTT, 'Gantt', 'macro'),
            new Zone(null, ZoneType.EPIC, 'EPICs', 'defaultEpicIconName'),
            new Zone(null, ZoneType.DEPENDENCY, 'Dependencies', 'defaultDependencyIconName'),
            new Zone(null, ZoneType.DEPENDENCY, "Another team's dependencies", 'server'),
        ];
    }
}