alter table exposition_storage.commit alter column message type character varying(1000000);

alter table job_storage.job alter column error type character varying(1000000);