ALTER TABLE utility ADD COLUMN geom_center geometry(Point,4326) null;
ALTER TABLE utility ADD CONSTRAINT enforce_dims_geom_center CHECK (st_ndims(geom_center) = 2);
ALTER TABLE utility ADD CONSTRAINT enforce_srid_geom_center CHECK (st_srid(geom_center) = 4326);

update utility set geom_center = (
	SELECT ST_Centroid(ST_ConvexHull(ST_Collect(m.location))) As the_geom 
	FROM device_meter As m
		inner join device d on m.id = d.id
			inner join account a on d.account_id = a.id
	where a.utility_id = utility.id
);

