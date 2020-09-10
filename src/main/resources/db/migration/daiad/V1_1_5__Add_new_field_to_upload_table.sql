alter table upload add row_negative_difference bigint;

update upload set row_negative_difference = 0;
