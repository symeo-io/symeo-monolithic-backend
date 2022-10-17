import Board from '../../../../src/core/board/Board';
import Dependency from '../../../../src/core/board/Dependency';
import Epic from '../../../../src/core/board/Epic';
import EpicTag from '../../../../src/core/board/EpicTag';
import Milestone from '../../../../src/core/board/Milestone';
import Sprint from '../../../../src/core/board/Sprint';
import SprintAssignation from '../../../../src/core/board/SprintAssignation';
import SprintDurationType from '../../../../src/core/board/SprintDurationType';
import User from '../../../../src/core/user/User';
import Zone from '../../../../src/core/board/Zone';
import ZoneDependencies from '../../../../src/core/board/ZoneDependencies';
import ZoneEpics from '../../../../src/core/board/ZoneEpics';
import ZoneType from '../../../../src/core/board/ZoneType';

const realDateNow = Date.now.bind(global.Date);

describe('Board', () => {
  beforeEach(() => {
    global.Date.now = jest.fn(() => 1546300800000); // 2019-01-01 00:00:00
  });

  afterEach(() => {
    global.Date.now = realDateNow;
  });

  describe('constructor', () => {
    it('should set the properties when given', () => {
      const milestone = new Milestone('id');
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('id', 0);
      const epicTag = new EpicTag('id', 'login', '#2e77bb');
      const members = ['memberId'];

      const board = new Board(
        'id',
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [milestone],
        members,
        [epicTag],
      );

      expect(board.id).toEqual('id');
      expect(board.name).toEqual('name');
      expect(board.slug).toEqual('slug');
      expect(board.slugId).toEqual('slugId');
      expect(board.startDate).toEqual(startDate);
      expect(board.sprints).toEqual([sprint]);
      expect(board.milestones).toEqual([milestone]);
      expect(board.members).toEqual(members);
      expect(board.epicTags).toEqual([epicTag]);
    });

    it('should generate id when given null value', () => {
      const milestone = new Milestone('id');
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('id', 0);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [milestone],
        [],
      );

      expect(board.id).toBeDefined();
    });

    it('should generate slug when given null value', () => {
      const milestone = new Milestone('id');
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('id', 0);

      const board = new Board(
        null,
        'My Super Board',
        null,
        null,
        startDate,
        null,
        null,
        null,
        [sprint],
        [milestone],
        [],
      );

      expect(board.slug).toEqual('my-super-board');
      expect(board.slugId).toBeDefined();
    });

    it('should init sprintDurationType to Week when given null value', () => {
      const milestone = new Milestone('id');
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('id', 0);
      const board = new Board(
        'id',
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [milestone],
        [],
      );

      expect(board.sprintDurationType).toEqual('Week');
    });

    it('should set sprintDuration to 1 when sprintDurationType is Day', () => {
      const milestone = new Milestone('id');
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('id', 0);
      const board = new Board(
        'id',
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        'Day',
        null,
        [sprint],
        [milestone],
        [],
      );

      expect(board.sprintDuration).toEqual(1);
    });

    it('should init sprints to empty array when given null value', () => {
      const milestone = new Milestone('id', null, null);
      const startDate = new Date('2018-01-01');

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        null,
        [milestone],
        [],
      );

      expect(board.sprints).toEqual([]);
    });

    it('should init milestones to empty array when given null value', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        null,
        [],
      );

      expect(board.milestones).toEqual([]);
    });

    it('should add default zone', () => {
      const board = new Board(null, 'name', 'slug', 'slugId', null, null, null, null, null, []);

      expect(board.zones.length).toEqual(4);
      expect(board.zones[0].type).toEqual(ZoneType.GANTT);
      expect(board.zones[0].name).toEqual('Gantt');
      expect(board.zones[0].iconName).toEqual('macro');
      expect(board.zones[1].type).toEqual(ZoneType.EPIC);
      expect(board.zones[1].name).toEqual('EPICs');
      expect(board.zones[1].iconName).toEqual('defaultEpicIconName');
      expect(board.zones[2].type).toEqual(ZoneType.DEPENDENCY);
      expect(board.zones[2].name).toEqual('Dependencies');
      expect(board.zones[2].iconName).toEqual('defaultDependencyIconName');
      expect(board.zones[3].type).toEqual(ZoneType.DEPENDENCY);
      expect(board.zones[3].name).toEqual("Another team's dependencies");
      expect(board.zones[3].iconName).toEqual('server');
    });
  });

  describe('addZone', () => {
    it('should add a zone correctly', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);

      const zone = new Zone(null, ZoneType.EPIC, 'name', 'icon', 'ownerEmail');

      expect(board.zones.length).toEqual(4);

      board.addZone(zone);
      expect(board.zones.length).toEqual(5);
      expect(board.zones[4]).toEqual(zone);
    });

    it('should create corresponding zones in sprints for epic', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      const zone = new Zone(null, ZoneType.EPIC, 'name', 'icon', 'ownerEmail');

      expect(board.sprints[0].zoneEpicsList.length).toEqual(0);

      board.addZone(zone);
      expect(board.sprints[0].zoneEpicsList.length).toEqual(1);
      expect(board.sprints[0].zoneEpicsList[0].zoneId).toEqual(zone.id);
    });

    it('should create corresponding zones in sprints for dependency', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      const zone = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      expect(board.sprints[0].zoneDependenciesList.length).toEqual(0);

      board.addZone(zone);
      expect(board.sprints[0].zoneDependenciesList.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[0].zoneId).toEqual(zone.id);
    });
  });

  describe('deleteZone', () => {
    it('should delete dependency zone', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      const zone = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(zone);
      expect(board.zones.length).toEqual(5);

      board.deleteZone(zone.id);
      expect(board.zones.length).toEqual(4);
    });

    it('should not delete epic zone', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      const zone = new Zone(null, ZoneType.EPIC, 'name', 'icon', 'ownerEmail');

      board.addZone(zone);
      expect(board.zones.length).toEqual(5);

      board.deleteZone(zone.id);
      expect(board.zones.length).toEqual(5);
    });

    it('should throw an error when trying to delete an non existing zone', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      expect(board.zones.length).toEqual(4);
      expect(() => board.deleteZone('fake_id')).toThrow(Error);
      expect(board.zones.length).toEqual(4);
    });

    it('should delete the dependency zones in sprints', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, []);

      const zone = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(zone);
      board.addSprint();

      expect(board.sprints[0].zoneDependenciesList.length).toEqual(3);
      board.deleteZone(zone.id);
      expect(board.sprints[0].zoneDependenciesList.length).toEqual(2);
    });

    it('should delete dependencies link to epic', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      const epicZone = new Zone(null, ZoneType.EPIC, 'name', 'icon', 'ownerEmail');
      const dependencyZone = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(epicZone);
      board.addZone(dependencyZone);

      board.sprints[0].zoneDependenciesList[0].addDependency('name', new Date(), false, 'email');
      board.sprints[0].zoneEpicsList[0].addEpic('name', 10, false, false);
      board.sprints[0].zoneEpicsList[0].epics[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      board.deleteZone(dependencyZone.id);
      expect(board.sprints[0].zoneEpicsList[0].epics[0].dependencies.length).toEqual(0);
    });

    it('should delete dependencies link to dependency', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint('id', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [sprint]);

      const dependencyZone1 = new Zone(null, ZoneType.DEPENDENCY, 'name1', 'icon', 'ownerEmail');
      const dependencyZone2 = new Zone(null, ZoneType.DEPENDENCY, 'name2', 'icon', 'ownerEmail');

      board.addZone(dependencyZone1);
      board.addZone(dependencyZone2);

      board.sprints[0].zoneDependenciesList[0].addDependency('name1', new Date(), false, 'email');
      board.sprints[0].zoneDependenciesList[1].addDependency('name2', new Date(), false, 'email');
      board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[1].dependencies[0].id,
      );

      board.deleteZone(dependencyZone2.id);
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });
  });

  describe('reorderZone', () => {
    it('should reorder zones', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);

      const epicZone = new Zone(null, ZoneType.EPIC, 'name', 'icon', 'ownerEmail');
      const dependencyZone = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(epicZone);
      board.addZone(dependencyZone);

      expect(board.zones.indexOf(epicZone)).toEqual(4);
      expect(board.zones.indexOf(dependencyZone)).toEqual(5);

      board.reorderZone(4, 5);
      expect(board.zones.indexOf(epicZone)).toEqual(5);
      expect(board.zones.indexOf(dependencyZone)).toEqual(4);
    });
  });

  describe('addSprint', () => {
    it('should add a sprint to sprint array at the end if no index is given', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);

      expect(board.sprints.length).toEqual(0);

      board.addSprint();
      expect(board.sprints.length).toEqual(1);
    });
    it('should add a sprint to sprint array at the given index', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);

      expect(board.sprints.length).toEqual(0);

      const firstSprint = new Sprint('id1');
      board.sprints.push(firstSprint);
      board.addSprint(0);
      expect(board.sprints.length).toEqual(2);
      expect(board.sprints[1].id).toEqual('id1');
    });
    it('should update dependencies date if necessary when sprintype is day and a next sprint is added', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, SprintDurationType.Day);
      const firstSprint = new Sprint('id1');
      const dependencyZone = new ZoneDependencies('zone1', 'zoneId1');
      const dependency = new Dependency('dependency', 'name', 42, startDate);
      dependencyZone.dependencies.push(dependency);
      firstSprint.addZoneDependencies('zoneId1', [dependency]);

      board.sprints.push(firstSprint);
      board.addSprint(0);
      expect(board.sprints.length).toEqual(2);
      expect(board.sprints[1].id).toEqual('id1');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].dueDate).toEqual(
        new Date('2018-01-02'),
      );
    });
    it('should update dependencies date if necessary when sprintype is week and a nex sprint is added', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        1,
        SprintDurationType.Week,
        1,
      );
      const firstSprint = new Sprint('id1');
      const dependencyZone = new ZoneDependencies('zone1', 'zoneId1');
      const dependency = new Dependency('dependency', 'name', 42, startDate);
      dependencyZone.dependencies.push(dependency);
      firstSprint.addZoneDependencies('zoneId1', [dependency]);

      board.sprints.push(firstSprint);
      board.addSprint(0);
      expect(board.sprints.length).toEqual(2);
      expect(board.sprints[1].id).toEqual('id1');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].dueDate).toEqual(
        new Date('2018-01-08'),
      );
    });
  });

  describe('removeSprint', () => {
    it('should remove sprint with id "sprint1"', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);
      const sprint1 = new Sprint('sprint1', 0);
      const sprint2 = new Sprint('sprint2', 0);
      board.sprints = [sprint1, sprint2];

      expect(board.sprints.length).toEqual(2);

      board.removeSprint('sprint1');
      expect(board.sprints.length).toEqual(1);
      expect(board.sprints).toEqual([sprint2]);
    });

    it('should remove the sprint dependencies from corresponding epics', () => {
      const startDate = new Date('2018-01-01');

      const firstSprint = new Sprint('id1', 0);
      const lastSprint = new Sprint('id2', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [
        firstSprint,
        lastSprint,
      ]);

      const epicZone = new Zone(null, ZoneType.EPIC, 'name', 'icon', 'ownerEmail');
      const dependencyZone = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(epicZone);
      board.addZone(dependencyZone);

      board.sprints[1].zoneDependenciesList[0].addDependency('name', new Date(), false, 'email');
      board.sprints[0].zoneEpicsList[0].addEpic('name', 10, false, false);
      board.sprints[0].zoneEpicsList[0].epics[0].dependencies.push(
        board.sprints[1].zoneDependenciesList[0].dependencies[0].id,
      );

      board.removeSprint('id2');
      expect(board.sprints[0].zoneEpicsList[0].epics[0].dependencies.length).toEqual(0);
    });

    it('should remove the sprint dependencies from corresponding dependencies', () => {
      const startDate = new Date('2018-01-01');

      const firstSprint = new Sprint('id1', 0);
      const lastSprint = new Sprint('id2', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, 1, null, 1, [
        firstSprint,
        lastSprint,
      ]);

      const dependencyZone1 = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');
      const dependencyZone2 = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(dependencyZone1);
      board.addZone(dependencyZone2);

      board.sprints[0].zoneDependenciesList[0].addDependency('name', startDate, false, 'email');
      board.sprints[1].zoneDependenciesList[1].addDependency(
        'name',
        new Date('2018-01-07'),
        false,
        'email',
      );
      board.sprints[1].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      board.removeSprint('id1');
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });

    it('should update dependency due date when a sprint before is removed and sprint type is day', () => {
      const startDate = new Date('2018-01-01');

      const firstSprint = new Sprint('id1', 0);
      const secondSprint = new Sprint('id2', 0);
      const lastSprint = new Sprint('id3', 0);
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        1,
        SprintDurationType.Day,
        1,
        [firstSprint, secondSprint, lastSprint],
      );

      const dependencyZone1 = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');
      const dependencyZone2 = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(dependencyZone1);
      board.addZone(dependencyZone2);

      board.sprints[0].zoneDependenciesList[0].addDependency('name', startDate, false, 'email');
      board.sprints[1].zoneDependenciesList[1].addDependency(
        'name',
        new Date('2018-01-02'),
        false,
        'email',
      );
      board.sprints[2].zoneDependenciesList[1].addDependency(
        'name',
        new Date('2018-01-03'),
        false,
        'email',
      );

      board.removeSprint('id2');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dueDate).toEqual(startDate);
      expect(board.sprints[1].zoneDependenciesList[1].dependencies[0].dueDate).toEqual(
        new Date('2018-01-02'),
      );
    });

    it('should update dependency due date when a sprint before is removed and sprint type is week and sprintduration 1', () => {
      const startDate = new Date('2018-01-01');

      const firstSprint = new Sprint('id1', 0);
      const secondSprint = new Sprint('id2', 0);
      const lastSprint = new Sprint('id3', 0);
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        1,
        SprintDurationType.Week,
        1,
        [firstSprint, secondSprint, lastSprint],
      );

      const dependencyZone1 = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');
      const dependencyZone2 = new Zone(null, ZoneType.DEPENDENCY, 'name', 'icon', 'ownerEmail');

      board.addZone(dependencyZone1);
      board.addZone(dependencyZone2);

      board.sprints[0].zoneDependenciesList[0].addDependency('name', startDate, false, 'email');
      board.sprints[1].zoneDependenciesList[1].addDependency(
        'name',
        new Date('2018-01-10'),
        false,
        'email',
      );
      board.sprints[2].zoneDependenciesList[1].addDependency(
        'name',
        new Date('2018-01-16'),
        false,
        'email',
      );

      board.removeSprint('id2');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dueDate).toEqual(startDate);
      expect(board.sprints[1].zoneDependenciesList[1].dependencies[0].dueDate).toEqual(
        new Date('2018-01-09'),
      );
    });
  });

  describe('moveEpic', () => {
    it('should move epic from on sprint to another', () => {
      const startDate = new Date('2018-01-01');
      const firstSprint = new Sprint('sprint-1', 0, [new ZoneEpics(null, 'fake_zone_id')]);
      const secondSprint = new Sprint('sprint-2', 0, [new ZoneEpics(null, 'fake_zone_id')]);

      firstSprint.addEpic('fake_zone_id', '1-1', 16, false, false);
      firstSprint.addEpic('fake_zone_id', '1-2', 16, false, false);
      secondSprint.addEpic('fake_zone_id', '2-1', 16, false, false);
      secondSprint.addEpic('fake_zone_id', '2-2', 16, false, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [firstSprint, secondSprint],
        [],
        [],
        [],
        [new Zone('fake_zone_id', ZoneType.EPIC)],
      );

      board.moveEpic('fake_zone_id', 'sprint-1', 'sprint-2', 0, 1);

      expect(board.sprints[0].zoneEpicsList[0].epics.length).toEqual(1);
      expect(board.sprints[1].zoneEpicsList[0].epics.length).toEqual(3);

      expect(board.sprints[0].zoneEpicsList[0].epics[0].name).toEqual('1-2');
      expect(board.sprints[1].zoneEpicsList[0].epics[0].name).toEqual('2-1');
      expect(board.sprints[1].zoneEpicsList[0].epics[1].name).toEqual('1-1');
      expect(board.sprints[1].zoneEpicsList[0].epics[2].name).toEqual('2-2');
    });

    it('should move epic into the same sprint', () => {
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('sprint-1', 0, [new ZoneEpics(null, 'fake_zone_id')]);

      sprint.addEpic('fake_zone_id', '1-1', 16, false, false);
      sprint.addEpic('fake_zone_id', '1-2', 16, false, false);
      sprint.addEpic('fake_zone_id', '1-3', 16, false, false);
      sprint.addEpic('fake_zone_id', '1-4', 16, false, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [new Zone('fake_zone_id', ZoneType.EPIC)],
      );

      board.moveEpic('fake_zone_id', 'sprint-1', 'sprint-1', 0, 1);

      expect(board.sprints[0].zoneEpicsList[0].epics.length).toEqual(4);

      expect(board.sprints[0].zoneEpicsList[0].epics[0].name).toEqual('1-2');
      expect(board.sprints[0].zoneEpicsList[0].epics[1].name).toEqual('1-1');
      expect(board.sprints[0].zoneEpicsList[0].epics[2].name).toEqual('1-3');
      expect(board.sprints[0].zoneEpicsList[0].epics[3].name).toEqual('1-4');
    });

    it('should fail if zone Id does not correspond to an existing epics zone', () => {
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('sprint-1', 0, [new ZoneEpics(null, 'fake_zone_id')]);

      sprint.addEpic('fake_zone_id', '1-1', 16, false, false);
      sprint.addEpic('fake_zone_id', '1-2', 16, false, false);
      sprint.addEpic('fake_zone_id', '1-3', 16, false, false);
      sprint.addEpic('fake_zone_id', '1-4', 16, false, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        // no EPIC zone in board
        [new Zone('fake_zone_id', ZoneType.DEPENDENCY)],
      );

      expect(() => {
        board.moveEpic('fake_zone_id', 'sprint-1', 'sprint-1', 0, 1);
      }).toThrow();
    });

    it('should fail when given non-existing sprint ids', () => {
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('sprint-1', 0);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [new Zone('fake_zone_id', ZoneType.EPIC)],
      );

      expect(() => {
        board.moveEpic('fake_zone_id', 'sprint-0', 'sprint-1', 0, 1);
      }).toThrow();

      expect(() => {
        board.moveEpic('fake_zone_id', 'sprint-1', 'sprint-0', 0, 1);
      }).toThrow();
    });
  });

  describe('moveDependency', () => {
    it('should move dependency from one sprint to another when sprint type is week', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const secondZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const firstSprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies]);
      const secondSprint = new Sprint('sprint-2', 0, [], [secondZoneDependencies]);

      firstSprint.addDependency('zone_id', '1-1', startDate, false);
      firstSprint.addDependency('zone_id', '1-2', startDate, false);
      secondSprint.addDependency('zone_id', '2-1', startDate, false);
      secondSprint.addDependency('zone_id', '2-2', startDate, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [firstSprint, secondSprint],
        [],
        [],
        [],
        [zone],
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-2', 0, 1);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies.length).toEqual(3);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].name).toEqual('1-2');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].name).toEqual('2-1');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[1].name).toEqual('1-1');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[2].name).toEqual('2-2');
    });

    it('should move epic into the same sprint when sprint type is week and zone are identical', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const zoneDependencies = new ZoneDependencies('id', 'zone_id');
      const sprint = new Sprint('sprint-1', 0, [], [zoneDependencies]);

      sprint.addDependency('zone_id', '1-1', startDate, false);
      sprint.addDependency('zone_id', '1-2', startDate, false);
      sprint.addDependency('zone_id', '1-3', startDate, false);
      sprint.addDependency('zone_id', '1-4', startDate, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [zone],
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-1', 0, 1);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(4);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].name).toEqual('1-2');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[1].name).toEqual('1-1');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[2].name).toEqual('1-3');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[3].name).toEqual('1-4');
    });

    it('should move dependency from one sprint to another when sprint type is day', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const secondZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const firstSprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies]);
      const secondSprint = new Sprint('sprint-2', 0, [], [secondZoneDependencies]);

      firstSprint.addDependency('zone_id', '1-1', startDate, false);
      firstSprint.addDependency('zone_id', '1-2', startDate, false);
      secondSprint.addDependency('zone_id', '2-1', startDate, false);
      secondSprint.addDependency('zone_id', '2-2', startDate, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        null,
        [firstSprint, secondSprint],
        [],
        [],
        [],
        [zone],
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-2', 0, 1);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies.length).toEqual(3);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].name).toEqual('1-2');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].name).toEqual('2-1');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[1].name).toEqual('1-1');
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[2].name).toEqual('2-2');
    });

    it('should move dependency from one zone to another, in the same sprint', () => {
      const startDate = new Date('2018-01-01');
      const sourceZone = new Zone('source_zone_id', ZoneType.DEPENDENCY, 'source zone name');
      const destinationZone = new Zone(
        'destination_zone_id',
        ZoneType.DEPENDENCY,
        'destination zone name',
      );
      const firstZoneDependencies = new ZoneDependencies('id1', 'source_zone_id');
      const secondZoneDependencies = new ZoneDependencies('id2', 'destination_zone_id');
      const sprint = new Sprint('sprint', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('source_zone_id', '1-1', startDate, false);
      sprint.addDependency('source_zone_id', '1-2', startDate, false);
      sprint.addDependency('destination_zone_id', '2-1', startDate, false);
      sprint.addDependency('destination_zone_id', '2-2', startDate, false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        null,
        [sprint],
        [],
        [],
        [],
        [sourceZone, destinationZone],
      );

      board.moveDependency('source_zone_id', 'destination_zone_id', 'sprint', 'sprint', 0, 1);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[1].dependencies.length).toEqual(3);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].name).toEqual('1-2');
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[0].name).toEqual('2-1');
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[1].name).toEqual('1-1');
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[2].name).toEqual('2-2');
    });

    it('should reverse dependency link (parent <-> child) if the dependencyDueDate become bigger than its parent', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const secondZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const thirdZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const firstSprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies]);
      const secondSprint = new Sprint('sprint-2', 0, [], [secondZoneDependencies]);
      const thirdSprint = new Sprint('sprint-3', 0, [], [thirdZoneDependencies]);

      firstSprint.addDependency('zone_id', '1-1', startDate, false);
      secondSprint.addDependency('zone_id', '2-1', new Date('2018-01-02'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        1,
        [firstSprint, secondSprint, thirdSprint],
        [],
        [],
        [],
        [zone],
      );

      board.sprints[1].zoneDependenciesList[0].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-3', 0, 0);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(0);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[2].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
      expect(board.sprints[2].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        1,
      );
    });

    it('should reverse dependency link (parent <-> child) if the dependencyDueDate become smaller than its child', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const secondZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const thirdZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const firstSprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies]);
      const secondSprint = new Sprint('sprint-2', 0, [], [secondZoneDependencies]);
      const thirdSprint = new Sprint('sprint-3', 0, [], [thirdZoneDependencies]);

      secondSprint.addDependency('zone_id', '2-1', new Date('2018-01-02'), false);
      thirdSprint.addDependency('zone_id', '3-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        1,
        [firstSprint, secondSprint, thirdSprint],
        [],
        [],
        [],
        [zone],
      );

      board.sprints[2].zoneDependenciesList[0].dependencies[0].dependencies.push(
        board.sprints[1].zoneDependenciesList[0].dependencies[0].id,
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-3', 'sprint-1', 0, 0);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[2].zoneDependenciesList[0].dependencies.length).toEqual(0);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        1,
      );
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });

    it('should not reverse dependency link (parent <-> child) if the dependencyDueDate stays bigger than its child', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const secondZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const thirdZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const firstSprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies]);
      const secondSprint = new Sprint('sprint-2', 0, [], [secondZoneDependencies]);
      const thirdSprint = new Sprint('sprint-3', 0, [], [thirdZoneDependencies]);

      firstSprint.addDependency('zone_id', '2-1', new Date('2018-01-02'), false);
      thirdSprint.addDependency('zone_id', '3-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        1,
        [firstSprint, secondSprint, thirdSprint],
        [],
        [],
        [],
        [zone],
      );

      board.sprints[2].zoneDependenciesList[0].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-3', 'sprint-2', 0, 0);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[2].zoneDependenciesList[0].dependencies.length).toEqual(0);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        1,
      );
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });

    it('should not reverse dependency link (parent <-> child) if the dependencyDueDate stays smaller than its parent', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const secondZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const thirdZoneDependencies = new ZoneDependencies('id', 'zone_id');
      const firstSprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies]);
      const secondSprint = new Sprint('sprint-2', 0, [], [secondZoneDependencies]);
      const thirdSprint = new Sprint('sprint-3', 0, [], [thirdZoneDependencies]);

      firstSprint.addDependency('zone_id', '2-1', new Date('2018-01-02'), false);
      thirdSprint.addDependency('zone_id', '3-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        1,
        [firstSprint, secondSprint, thirdSprint],
        [],
        [],
        [],
        [zone],
      );

      board.sprints[2].zoneDependenciesList[0].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-2', 0, 0);

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(0);
      expect(board.sprints[1].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[2].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[2].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        1,
      );
      expect(board.sprints[1].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });

    it('should throw when given non-existing source zone id', () => {
      const startDate = new Date('2018-01-01');
      const firstSprint = new Sprint('sprint-1', 0);
      const secondSprint = new Sprint('sprint-2', 0);

      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, null, [
        firstSprint,
        secondSprint,
      ]);

      expect(() =>
        board.moveDependency('zone_id', 'zone_id', 'sprint-0', 'sprint-1', 0, 1),
      ).toThrow();
      expect(() =>
        board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-0', 0, 1),
      ).toThrow();
    });

    it('should throw when given non-existing destination zone id', () => {
      const startDate = new Date('2018-01-01');
      const source_zone = new Zone('source_zone_id', ZoneType.DEPENDENCY, 'source zone');
      const firstSprint = new Sprint('sprint-1', 0);
      const secondSprint = new Sprint('sprint-2', 0);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [firstSprint, secondSprint],
        [],
        [],
        [],
        [source_zone],
      );

      expect(() =>
        board.moveDependency('source_zone_id', 'zone_id', 'sprint-0', 'sprint-1', 0, 1),
      ).toThrow();
      expect(() =>
        board.moveDependency('source_zone_id', 'zone_id', 'sprint-1', 'sprint-0', 0, 1),
      ).toThrow();
    });

    it('should throw when given non-existing sprint ids', () => {
      const startDate = new Date('2018-01-01');
      const zone = new Zone('zone_id', ZoneType.DEPENDENCY, 'zone name');
      const sprint = new Sprint('sprint-1', 0);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [zone],
      );

      expect(() =>
        board.moveDependency('zone_id', 'zone_id', 'sprint-0', 'sprint-1', 0, 1),
      ).toThrow();
      expect(() =>
        board.moveDependency('zone_id', 'zone_id', 'sprint-1', 'sprint-0', 0, 1),
      ).toThrow();
    });
  });

  describe('deleteDependency', () => {
    it('should delete dependency', () => {
      const startDate = new Date('2018-01-01');
      const epicZone = new Zone('epic_zone_id', ZoneType.EPIC, 'epic zone');
      const dependencyZone = new Zone('dependency_zone_id', ZoneType.DEPENDENCY, 'dependency zone');
      const dependency = new Dependency('dep', 'dep');
      const epic = new Epic('epic', 'epic', 5, false, false, ['dep']);
      const zoneEpics = new ZoneEpics('zone_epics_id', 'epic_zone_id', [epic]);
      const zoneDependencies = new ZoneDependencies('zone_dependencies_id', 'dependency_zone_id', [
        dependency,
      ]);
      const sprint = new Sprint('sprint-1', 0, [zoneEpics], [zoneDependencies]);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [epicZone, dependencyZone],
      );

      board.deleteDependency('dependency_zone_id', 'sprint-1', 'dep');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toBe(0);
      expect(board.sprints[0].zoneEpicsList[0].epics[0].dependencies.length).toBe(0);
    });

    it('should delete dependency and delete dependency from dependency', () => {
      const startDate = new Date('2018-01-01');
      const epicZone = new Zone('epic_zone_id', ZoneType.EPIC, 'epic zone');
      const dependencyZone = new Zone('dependency_zone_id', ZoneType.DEPENDENCY, 'dependency zone');
      const dependencyChild = new Dependency('depChild', 'depChild');
      const dependencyParent = new Dependency(
        'depParent',
        'depParent',
        42,
        new Date('2018-01-01'),
        false,
        '',
        ['depChild'],
      );
      const zoneDependencies = new ZoneDependencies('zone_dependencies_id', 'dependency_zone_id', [
        dependencyChild,
        dependencyParent,
      ]);
      const sprint = new Sprint('sprint-1', 0, [], [zoneDependencies]);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [epicZone, dependencyZone],
      );

      board.deleteDependency('dependency_zone_id', 'sprint-1', 'depChild');
      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toBe(1);
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toBe(0);
    });

    it('should throw when given a non-existing zone id', () => {
      const startDate = new Date('2018-01-01');
      const sprint = new Sprint('sprint-1', 0);
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, null, [
        sprint,
      ]);

      expect(() => {
        board.deleteDependency('dependency_zone_id', 'sprint-0', 'dep');
      }).toThrow();
    });

    it('should throw when given a non-existing sprint id', () => {
      const startDate = new Date('2018-01-01');
      const zoneDependencies = new ZoneDependencies(
        'zone_dependencies_id',
        'dependency_zone_id',
        [],
      );
      const sprint = new Sprint('sprint-1', 0, [], [zoneDependencies]);
      const dependencyZone = new Zone('dependency_zone_id', ZoneType.DEPENDENCY, 'dependency zone');
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        null,
        [sprint],
        [],
        [],
        [],
        [dependencyZone],
      );

      expect(() => {
        board.deleteDependency('dependency_zone_id', 'sprint-0', 'dep');
      }).toThrow();
    });
  });

  describe('getCurrentSprintIndex', () => {
    it('should return -1 when board start date is in the future', () => {
      const startDate = new Date('2020-01-01');
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, null, []);

      expect(board.currentSprintIndex).toBe(-1);
    });

    it('should return the index of the sprint when sprints last 1 week', () => {
      const startDate = new Date('2018-12-01');
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, 1, []);

      expect(board.currentSprintIndex).toBe(4);
    });

    it('should return the index of the sprint when sprints last 2 weeks', () => {
      const startDate = new Date('2018-12-01');
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, 2, []);

      expect(board.currentSprintIndex).toBe(2);
    });

    it('should return the index of the sprint when sprints last 3 weeks', () => {
      const startDate = new Date('2018-12-01');
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, 3, []);

      expect(board.currentSprintIndex).toBe(1);
    });

    it('should return the index of the sprint when sprints last 1 day', () => {
      const startDate = new Date('2018-12-31');
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        1,
        [],
      );

      expect(board.currentSprintIndex).toBe(1);
    });
  });

  describe('addMember', () => {
    it('should add member to board', () => {
      const startDate = new Date('2018-12-01');
      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, 3, [], [], []);

      board.addMember(new User('user_id', 'member@domain.com'));
      expect(board.members.length).toBe(1);
      expect(board.members[0]).toEqual('member@domain.com');
    });

    it('should not do anything if user is already a member', () => {
      const startDate = new Date('2018-12-01');
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        3,
        [],
        [],
        ['member@domain.com'],
      );

      board.addMember(new User('user_id', 'member@domain.com'));
      expect(board.members.length).toBe(1);
      expect(board.members[0]).toEqual('member@domain.com');
    });
  });

  describe('removeMember', () => {
    it('should remove member from board', () => {
      const startDate = new Date('2018-12-01');
      const sprint = new Sprint('sprint_id', 10, [], [], []);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        3,
        [sprint],
        [],
        ['member@domain.com'],
      );

      board.removeMember(new User('user_id', 'member@domain.com'));
      expect(board.members.length).toBe(0);
      expect(board.sprints[0].assignees.length).toBe(0);
    });

    it('should remove member from board and its assignations if any', () => {
      const startDate = new Date('2018-12-01');
      const assignee = new SprintAssignation('member@domain.com');
      const sprint = new Sprint('sprint_id', 10, [], [], [assignee]);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        3,
        [sprint],
        [],
        ['member@domain.com'],
      );

      board.removeMember(new User('user_id', 'member@domain.com'));
      expect(board.members.length).toBe(0);
      expect(board.sprints[0].assignees.length).toBe(0);
    });

    it('should not do anything if user is not a member', () => {
      const startDate = new Date('2018-12-01');
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        3,
        [],
        [],
        ['member@domain.com'],
      );

      board.removeMember(new User('user_id_2', 'not-a-member@domain.com'));
      expect(board.members.length).toBe(1);
      expect(board.members[0]).toEqual('member@domain.com');
    });
  });

  describe('epicTag', () => {
    describe('delete', () => {
      it('should remove epicTag from board', () => {
        const startDate = new Date('2018-12-01');
        const board = new Board(
          null,
          'name',
          'slug',
          'slugId',
          startDate,
          null,
          null,
          3,
          [],
          [],
          [],
          [new EpicTag('macro_epic_id')],
        );

        board.deleteEpicTag('macro_epic_id');
        expect(board.epicTags.length).toBe(0);
      });
    });
    describe('update', () => {
      it('should update an epic tag with the given properties', () => {
        const startDate = new Date('2018-12-01');
        const tagId = 'macro_epic_id';
        const tagToEdit = new EpicTag(tagId);
        const board = new Board(
          null,
          'name',
          'slug',
          'slugId',
          startDate,
          null,
          null,
          3,
          [],
          [],
          [],
          [tagToEdit],
        );

        board.updateEpicTag('macro_epic_id', 'new name', 'blue');
        expect(tagToEdit.name).toEqual('new name');
        expect(tagToEdit.color).toEqual('blue');
      });
    });
    describe('delete', () => {
      it('should delete an epic tag', () => {
        const startDate = new Date('2018-12-01');
        const tagId = 'macro_epic_id';
        const tagToEdit = new EpicTag(tagId);
        const epic = new Epic('epic', 'epic', 5, false, false, [], 'macro_epic_id');
        const zoneEpics = new ZoneEpics('zone_epics_id', 'epic_zone_id', [epic]);
        const sprint = new Sprint('sprint-1', 0, [zoneEpics], []);

        const board = new Board(
          null,
          'name',
          'slug',
          'slugId',
          startDate,
          null,
          null,
          3,
          [sprint],
          [],
          [],
          [tagToEdit],
        );

        board.deleteEpicTag('macro_epic_id');
        expect(board.epicTags.length).toEqual(0);
        expect(epic.epicTagId).toEqual('');
      });
    });
  });

  describe('applyStaffingToFollowingSprints', () => {
    it('should apply staffing to all following sprints', () => {
      const startDate = new Date('2018-12-01');
      const sprint1 = new Sprint(null, null, null, null, [
        new SprintAssignation('user@domain.com', 1),
      ]);
      const sprint2 = new Sprint(null, null, null, null, [
        new SprintAssignation('user2@domain.com', 1),
      ]);
      const sprint3 = new Sprint(null, null, null, null, []);
      const sprint4 = new Sprint(null, null, null, null, []);

      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, 3, [
        sprint1,
        sprint2,
        sprint3,
        sprint4,
      ]);

      board.applyStaffingToFollowingSprints(1);

      expect(board.sprints[2].assignees.length).toBe(1);
      expect(board.sprints[2].assignees[0].email).toEqual('user2@domain.com');
      expect(board.sprints[2].assignees[0].staffing).toEqual(1);

      expect(board.sprints[3].assignees.length).toBe(1);
      expect(board.sprints[3].assignees[0].email).toEqual('user2@domain.com');
      expect(board.sprints[3].assignees[0].staffing).toEqual(1);
    });

    it('should not do anything if sprint index does not exist', () => {
      const startDate = new Date('2018-12-01');
      const sprint1 = new Sprint(null, null, null, null, [
        new SprintAssignation('user@domain.com', 1),
      ]);
      const sprint2 = new Sprint(null, null, null, null, [
        new SprintAssignation('user2@domain.com', 1),
      ]);
      const sprint3 = new Sprint(null, null, null, null, []);
      const sprint4 = new Sprint(null, null, null, null, []);

      const board = new Board(null, 'name', 'slug', 'slugId', startDate, null, null, 3, [
        sprint1,
        sprint2,
        sprint3,
        sprint4,
      ]);

      board.applyStaffingToFollowingSprints(10);

      expect(board.sprints[0].assignees.length).toBe(1);
      expect(board.sprints[1].assignees.length).toBe(1);
      expect(board.sprints[2].assignees.length).toBe(0);
      expect(board.sprints[3].assignees.length).toBe(0);
    });
  });

  describe('addEpicTag', () => {
    it('should add a epicTag to epicTags array', () => {
      const startDate = new Date('2018-01-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);

      expect(board.epicTags.length).toEqual(0);

      board.addEpicTag('logout', '#b712a1');
      expect(board.epicTags.length).toEqual(1);
    });
  });

  describe('createMilestone', () => {
    it('should add a milestone to milestones array', () => {
      const startDate = new Date('2018-10-01');

      const board = new Board(null, 'name', 'slug', 'slugId', startDate);

      expect(board.milestones.length).toEqual(0);

      const milestoneDate = new Date('2018-10-02');

      board.createMilestone('milestone', milestoneDate);
      expect(board.milestones.length).toEqual(1);
      expect(board.milestones[0].name).toBe('milestone');
      expect(board.milestones[0].date).toBe(milestoneDate);
    });
  });

  describe('deleteMilestone', () => {
    it('should remove milestone from board', () => {
      const startDate = new Date('2018-12-01');
      const milestoneDate = new Date('2018-12-02');
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        null,
        3,
        [],
        [new Milestone('milestone_id', 'milestone', milestoneDate)],
        [],
        [],
      );

      board.deleteMilestone('milestone_id');
      expect(board.milestones.length).toBe(0);
    });
  });

  describe('fillBoardWithDefaultData', () => {
    it('should fill board with default data', () => {
      const startDate = new Date('2018-12-01');
      const milestoneDate = new Date('2018-12-02');
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Day,
        3,
        [],
        [new Milestone('milestone_id', 'milestone', milestoneDate)],
        [],
        [],
      );

      board.fillBoardWithDefaultData();

      expect(board.sprints.length).toBe(4);
      // TODO: add more exhaustive expects
    });
  });

  describe('duplicate', () => {
    it('should duplicate the board keeping all the members', () => {
      const startDate = new Date('2018-01-01');

      const sprint = new Sprint(null, null, null, null, []);
      const milestone = new Milestone();
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        5,
        null,
        1,
        [sprint],
        [milestone],
        ['member@domain.com', 'othermember@domain.com'],
        [new EpicTag('macro_id', 'macro_name')],
        [new Zone('1', null, 'TestZone', null, 'othermember@domain.com')],
      );
      const duplicate = board.duplicate('member@domain.com', 'name - Copy', true);

      expect(duplicate).not.toEqual(board);
      expect(duplicate.id).not.toEqual(board.id);
      expect(duplicate.name).toEqual('name - Copy');
      expect(duplicate.startDate).toEqual(board.startDate);
      expect(duplicate.firstSprintNumber).toEqual(board.firstSprintNumber);
      expect(duplicate.sprintDuration).toEqual(board.sprintDuration);
      expect(duplicate.sprints.length).toEqual(board.sprints.length);
      expect(duplicate.milestones.length).toEqual(board.milestones.length);
      expect(duplicate.members).toEqual(board.members);
      expect(duplicate.epicTags.length).toEqual(board.epicTags.length);
      expect(duplicate.sprints[0].id).not.toEqual(board.sprints[0].id);
      expect(duplicate.zones[0].ownerEmail).toEqual('othermember@domain.com');
    });
  });

  describe('duplicate', () => {
    it('should duplicate the board keeping just the current member', () => {
      const startDate = new Date('2018-01-01');
      const sprintAssignation = new SprintAssignation('member@domain.com', 3);
      const sprint = new Sprint(null, null, null, null, [sprintAssignation]);
      const milestone = new Milestone();
      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        5,
        null,
        1,
        [sprint],
        [milestone],
        ['member@domain.com', 'othermember@domain.com'],
        [new EpicTag('macro_id', 'macro_name')],
        [new Zone('1', null, 'TestZone', null, 'othermember@domain.com')],
      );
      const duplicate = board.duplicate('member@domain.com', 'name - Copy', false);

      expect(duplicate).not.toEqual(board);
      expect(duplicate.id).not.toEqual(board.id);
      expect(duplicate.name).toEqual('name - Copy');
      expect(duplicate.startDate).toEqual(board.startDate);
      expect(duplicate.firstSprintNumber).toEqual(board.firstSprintNumber);
      expect(duplicate.sprintDuration).toEqual(board.sprintDuration);
      expect(duplicate.sprints.length).toEqual(board.sprints.length);
      expect(duplicate.milestones.length).toEqual(board.milestones.length);
      expect(duplicate.members).toEqual(['member@domain.com']);
      expect(duplicate.epicTags.length).toEqual(board.epicTags.length);
      expect(duplicate.sprints[0].id).not.toEqual(board.sprints[0].id);
      expect(duplicate.sprints[0].assignees).toEqual([]);
      expect(duplicate.zones[0].ownerEmail).toEqual('');
    });
  });

  describe('generateSlug', () => {
    it('should generate a slug from the board name', () => {
      expect(Board.generateSlug('Nom du Board')).toEqual('nom-du-board');
    });
    it('should return null if no name is given', () => {
      expect(Board.generateSlug(null)).toEqual(null);
    });
  });

  describe('generateNewIds', () => {
    it('should generate a new id', () => {
      const newIds = {};
      const id1 = Board.translateId(newIds)('a');
      const id2 = Board.translateId(newIds)('b');
      expect(id1).not.toEqual(id2);
    });
    it('should return the same id as previously', () => {
      const newIds = {};
      const id1 = Board.translateId(newIds)('a');
      const id2 = Board.translateId(newIds)('a');
      expect(id1).toEqual(id2);
    });
    it('should return null if no id is given', () => {
      const newIds = {};
      expect(Board.translateId(newIds)(null)).toEqual(null);
    });
  });
  describe('updateSprintDependencyLinksOnEdit', () => {
    it('should reverse dependency link (parent <-> child) if the dependencyDueDate become bigger than its parent after edit', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', startDate, false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[0].dependencies[0].dueDate = new Date('2018-01-05');
      board.updateSprintDependencyLinksOnEdit(
        'sprint-1',
        'zone_id1',
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[1].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        1,
      );
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });

    it('should reverse dependency link (parent <-> child) if the dependencyDueDate become smaller than its child', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', new Date('2018-01-03'), false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-05'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[1].dependencies[0].dueDate = new Date('2018-01-01');
      board.updateSprintDependencyLinksOnEdit(
        'sprint-1',
        'zone_id2',
        board.sprints[0].zoneDependenciesList[1].dependencies[0].id,
      );

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[1].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        1,
      );
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.length).toEqual(
        0,
      );
    });

    it('should not reverse dependency link (parent <-> child) if the dependencyDueDate stays bigger than its child', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', startDate, false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[0].dependencies[0].dueDate = new Date('2018-01-02');
      board.updateSprintDependencyLinksOnEdit(
        'sprint-1',
        'zone_id1',
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[1].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.length).toEqual(
        1,
      );
    });

    it('should not reverse dependency link (parent <-> child) if the dependencyDueDate stays smaller than its parent', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', startDate, false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[1].dependencies[0].dueDate = new Date('2018-01-02');
      board.updateSprintDependencyLinksOnEdit(
        'sprint-1',
        'zone_id2',
        board.sprints[0].zoneDependenciesList[1].dependencies[0].id,
      );

      expect(board.sprints[0].zoneDependenciesList[0].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[1].dependencies.length).toEqual(1);
      expect(board.sprints[0].zoneDependenciesList[0].dependencies[0].dependencies.length).toEqual(
        0,
      );
      expect(board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.length).toEqual(
        1,
      );
    });

    it('should throw an error when given false sprint id', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', startDate, false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[1].dependencies[0].dueDate = new Date('2018-01-02');

      expect(() =>
        board.updateSprintDependencyLinksOnEdit(
          'sprint',
          'zone_id2',
          board.sprints[0].zoneDependenciesList[1].dependencies[0].id,
        ),
      ).toThrow();
    });

    it('should throw an error when given false zoneId', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', startDate, false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[1].dependencies[0].dueDate = new Date('2018-01-02');

      expect(() =>
        board.updateSprintDependencyLinksOnEdit(
          'sprint-1',
          'zone_id0',
          board.sprints[0].zoneDependenciesList[1].dependencies[0].id,
        ),
      ).toThrow();
    });

    it('should throw an error when given false dependencyId', () => {
      const startDate = new Date('2018-01-01');
      const zone1 = new Zone('zone_id1', ZoneType.DEPENDENCY, 'zone name');
      const zone2 = new Zone('zone_id2', ZoneType.DEPENDENCY, 'zone name');
      const firstZoneDependencies = new ZoneDependencies('id1', 'zone_id1');
      const secondZoneDependencies = new ZoneDependencies('id2', 'zone_id2');
      const sprint = new Sprint('sprint-1', 0, [], [firstZoneDependencies, secondZoneDependencies]);

      sprint.addDependency('zone_id1', '1-1', startDate, false);
      sprint.addDependency('zone_id2', '2-1', new Date('2018-01-03'), false);

      const board = new Board(
        null,
        'name',
        'slug',
        'slugId',
        startDate,
        null,
        SprintDurationType.Week,
        1,
        [sprint],
        [],
        [],
        [],
        [zone1, zone2],
      );

      board.sprints[0].zoneDependenciesList[1].dependencies[0].dependencies.push(
        board.sprints[0].zoneDependenciesList[0].dependencies[0].id,
      );
      board.sprints[0].zoneDependenciesList[1].dependencies[0].dueDate = new Date('2018-01-02');

      expect(() =>
        board.updateSprintDependencyLinksOnEdit('sprint-1', 'zone_id1', 'dependency'),
      ).toThrow();
    });
  });
});
