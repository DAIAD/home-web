-- Drop existing tables
DROP TABLE IF EXISTS public.group_set;

DROP TABLE IF EXISTS public.group_cluster;

CREATE TABLE group_set (
	id integer NOT NULL,
    owner_id int,
    CONSTRAINT pk_group_set PRIMARY KEY (id),
    CONSTRAINT fk_group_set_group FOREIGN KEY (id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_group_set_account FOREIGN KEY (owner_id)
        REFERENCES public.account (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE group_segment (
	id integer NOT NULL,
    cluster_id int,
    CONSTRAINT pk_group_segment PRIMARY KEY (id),
    CONSTRAINT fk_group_segment_group FOREIGN KEY (id)
        REFERENCES public."group" (id) MATCH SIMPLE
            ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT fk_group_segment_cluster FOREIGN KEY (cluster_id)
		REFERENCES public."cluster" (id) MATCH SIMPLE
			ON UPDATE CASCADE ON DELETE CASCADE
);
