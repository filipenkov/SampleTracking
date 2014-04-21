--replace all quotes once copied and pasted
--current list of states
--AK AL AR AZ CA CO CT DC DE FL GA HI IA ID IL IN KS KY LA 
--MA MD ME MI MN MO MS MT NC ND NE NJ NM NY OH OK OR PA PR RI 
--SC SD TN TX VA VT WA WI WY
--Missing states 
--NV Nevada
--NH New Hampshire
--UT Utah
--WV West Virginia 

--DROP TABLE #state_map;
CREATE TABLE #state_map (
  district VARCHAR(80) NULL, 
  state   VARCHAR(8)
);

INSERT INTO #state_map(district, state) values ("	1319	","	Unknown	");
INSERT INTO #state_map(district, state) values ("	2151	","	Unknown	");
INSERT INTO #state_map(district, state) values ("	Jamesburg	","	Unknown	");
INSERT INTO #state_map(district, state) values ("	Lexington	","	Unknown	");
INSERT INTO #state_map(district, state) values ("	Vanderbilt	","	Unknown	");
INSERT INTO #state_map(district, state) values ("	Weiss	","	Unknown	");

INSERT INTO #state_map(district, state) values ("	N/A	","	Unknown	");
INSERT INTO #state_map(district, state) values ("	Puerto Rico	","	PR	");


INSERT INTO #state_map(district, state) values ("	AK	","	AK	");
INSERT INTO #state_map(district, state) values ("	Alaska	","	AK	");
INSERT INTO #state_map(district, state) values ("	Cordova, Southcentral Alaska	","	AK	");
INSERT INTO #state_map(district, state) values ("	Minto Flats, Interior Alaska	","	AK	");
INSERT INTO #state_map(district, state) values ("	Monroe county, AK	","	AK	");

INSERT INTO #state_map(district, state) values ("	Bellamy, AL	","	AL	");
INSERT INTO #state_map(district, state) values ("	Columbiana, AL	","	AL	");
INSERT INTO #state_map(district, state) values ("	Madison, AL	","	AL	");

INSERT INTO #state_map(district, state) values ("	Arkansas	","	AR	");
INSERT INTO #state_map(district, state) values ("	Arkansas county, AK	","	AR	");

INSERT INTO #state_map(district, state) values ("	Arizona	","	AZ	");

INSERT INTO #state_map(district, state) values ("	Alameda	","	CA	");
INSERT INTO #state_map(district, state) values ("	Berkeley, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Brawley, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Butte County, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Calexico, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Colusa County, California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Contra Costa	","	CA	");
INSERT INTO #state_map(district, state) values ("	El Centro, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Fresno	","	CA	");
INSERT INTO #state_map(district, state) values ("	Fresno County, California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Glenn County, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Glenn County, California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Granada Hills, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Gridley, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Hacienda Heights, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Los Angeles, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Marin	","	CA	");
INSERT INTO #state_map(district, state) values ("	Merced County, California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Mono	","	CA	");
INSERT INTO #state_map(district, state) values ("	Orange County, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Perris, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Placer	","	CA	");
INSERT INTO #state_map(district, state) values ("	Sacramento	","	CA	");
INSERT INTO #state_map(district, state) values ("	San Diego, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	San Francisco	","	CA	");
INSERT INTO #state_map(district, state) values ("	San Joaquin County, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	San Luis Obispo	","	CA	");
INSERT INTO #state_map(district, state) values ("	Santa Clara	","	CA	");
INSERT INTO #state_map(district, state) values ("	Siskiyou County, California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Solano County, CA	","	CA	");
INSERT INTO #state_map(district, state) values ("	Sutter County, California	","	CA	");
INSERT INTO #state_map(district, state) values ("	Yolo	","	CA	");
INSERT INTO #state_map(district, state) values ("	Yolo County, CA	","	CA	");

INSERT INTO #state_map(district, state) values ("	Colorado	","	CO	");
INSERT INTO #state_map(district, state) values ("	Denver, CO	","	CO	");
INSERT INTO #state_map(district, state) values ("	Louisville, CO	","	CO	");

INSERT INTO #state_map(district, state) values ("	Connecticut	","	CT	");

INSERT INTO #state_map(district, state) values ("	Washington, DC	","	DC	");

INSERT INTO #state_map(district, state) values ("	Bower's Beach, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	Delaware	","	DE	");
INSERT INTO #state_map(district, state) values ("	Delaware Bay, Delaware	","	DE	");
INSERT INTO #state_map(district, state) values ("	Mispillion Beach, Sussex county, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	Mispillion Dock, Sussex county, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	Mispillion Harbor, Sussex county, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	Port Mahon, Kent county, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	Slaughter Beach, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	South Bower's Beach, DE	","	DE	");
INSERT INTO #state_map(district, state) values ("	Ted Harvey Wildlife Area, DE	","	DE	");

INSERT INTO #state_map(district, state) values ("	Florida	","	FL	");
INSERT INTO #state_map(district, state) values ("	Pensacola, FL	","	FL	");
INSERT INTO #state_map(district, state) values ("	St. Peterburg, FL	","	FL	");
INSERT INTO #state_map(district, state) values ("	St. Petersburg, FL	","	FL	");

INSERT INTO #state_map(district, state) values ("	Atlanta	","	GA	");
INSERT INTO #state_map(district, state) values ("	Atlanta, GA	","	GA	");
INSERT INTO #state_map(district, state) values ("	Georgia	","	GA	");
INSERT INTO #state_map(district, state) values ("	Muscogee County, GA	","	GA	");
INSERT INTO #state_map(district, state) values ("	Muscogee county, GA	","	GA	");
INSERT INTO #state_map(district, state) values ("	Superior Landfill, GA	","	GA	");

INSERT INTO #state_map(district, state) values ("	Hawaii	","	HI	");

INSERT INTO #state_map(district, state) values ("	Iowa	","	IA	");

INSERT INTO #state_map(district, state) values ("	Idaho	","	ID	");

INSERT INTO #state_map(district, state) values ("	Bloomingdale, IL	","	IL	");
INSERT INTO #state_map(district, state) values ("	Dunlap, IL	","	IL	");
INSERT INTO #state_map(district, state) values ("	Illinois	","	IL	");
INSERT INTO #state_map(district, state) values ("	Illinois, Rockville	","	IL	");
INSERT INTO #state_map(district, state) values ("	Lake County, IL	","	IL	");
INSERT INTO #state_map(district, state) values ("	Lake county, IL	","	IL	");
INSERT INTO #state_map(district, state) values ("	Lake Zurich, IL	","	IL	");
INSERT INTO #state_map(district, state) values ("	Naperville, IL	","	IL	");
INSERT INTO #state_map(district, state) values ("	Perin, IL	","	IL	");

INSERT INTO #state_map(district, state) values ("	Indiana	","	IN	");

INSERT INTO #state_map(district, state) values ("	Kansas	","	KS	");
INSERT INTO #state_map(district, state) values ("	Overland Park, KS	","	KS	");

INSERT INTO #state_map(district, state) values ("	Florence, KY	","	KY	");
INSERT INTO #state_map(district, state) values ("	Hopkinsville, KY	","	KY	");
INSERT INTO #state_map(district, state) values ("	Kentucky	","	KY	");

INSERT INTO #state_map(district, state) values ("	Louisiana	","	LA	");
INSERT INTO #state_map(district, state) values ("	Cameron Parish, LA	","	LA	");
INSERT INTO #state_map(district, state) values ("	Hackberry, Cameron Parish LA	","	LA	");

INSERT INTO #state_map(district, state) values ("	Boston	","	MA	");
INSERT INTO #state_map(district, state) values ("	Boston, MA	","	MA	");
INSERT INTO #state_map(district, state) values ("	Cambridge, MA	","	MA	");
INSERT INTO #state_map(district, state) values ("	Massachusetts	","	MA	");
INSERT INTO #state_map(district, state) values ("	Massachusetts, Boston	","	MA	");
INSERT INTO #state_map(district, state) values ("	Springfield, MA	","	MA	");

INSERT INTO #state_map(district, state) values ("	Baltimore County, MD	","	MD	");
INSERT INTO #state_map(district, state) values ("	Maryland	","	MD	");
INSERT INTO #state_map(district, state) values ("	MD	","	MD	");

INSERT INTO #state_map(district, state) values ("	Maine	","	ME	");

INSERT INTO #state_map(district, state) values ("	Ann Arbor, MI	","	MI	");
INSERT INTO #state_map(district, state) values ("	Ann Arbor, Michigan	","	MI	");
INSERT INTO #state_map(district, state) values ("	Michigan	","	MI	");
INSERT INTO #state_map(district, state) values ("	Michigan, Ann Arbor	","	MI	");
INSERT INTO #state_map(district, state) values ("	Royal Oak, MI	","	MI	");

INSERT INTO #state_map(district, state) values ("	Minnesota	","	MN	");
INSERT INTO #state_map(district, state) values ("	Rochester, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau River, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Agassiz NWR, Agassiz Pool, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Agassiz NWR, Farmes Pool, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Agassiz NWR, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Agassiz NWR, Parker Pool, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Agassiz NWR, Tamarac Pool, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Agassiz NWR, Webster Creek Pool, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Marshall/Roseau county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau Pool#1, Roseau county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau Pool#3, Roseau county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau River Peninsula, Roseau county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Roseau River, Roseau county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Thief Lake, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Thief Lake, NW, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Thief Lake, River Site, Marshall county, MN	","	MN	");
INSERT INTO #state_map(district, state) values ("	Thief River, Marshall county, MN	","	MN	");

INSERT INTO #state_map(district, state) values ("	Missouri	","	MO	");
INSERT INTO #state_map(district, state) values ("	MO	","	MO	");
INSERT INTO #state_map(district, state) values ("	Pulaski County, MO	","	MO	");
INSERT INTO #state_map(district, state) values ("	Pulaski county, MO	","	MO	");

INSERT INTO #state_map(district, state) values ("	Aberdeen, MS	","	MS	");
INSERT INTO #state_map(district, state) values ("	Mississippi	","	MS	");

INSERT INTO #state_map(district, state) values ("	Butte	","	MT	");

INSERT INTO #state_map(district, state) values ("	Graham, NC	","	NC	");
INSERT INTO #state_map(district, state) values ("	North Carolina	","	NC	");
INSERT INTO #state_map(district, state) values ("	Mattamuskeet NWR, NC	","	NC	");
INSERT INTO #state_map(district, state) values ("	Winston-Salem, NC	","	NC	");
INSERT INTO #state_map(district, state) values ("	JM Futch, NC	","	NC	");

INSERT INTO #state_map(district, state) values ("	Ilo, Dunn county, ND	","	ND	");
INSERT INTO #state_map(district, state) values ("	Lake Audobon, McLean county, ND	","	ND	");

INSERT INTO #state_map(district, state) values ("	Nebraska	","	NE	");

INSERT INTO #state_map(district, state) values ("	Cape May, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Delaware Bay, New Jersey	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Delaware Bay, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Fort Monmouth, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	New Jersey	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Newark, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Atlantic county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Big Muddle Hole Island, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Burlington county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Cook's Beach, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Fortescue Beach, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Gandy's Beach, Cumberland county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Helmetta, Middlesex county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Howard Stainton, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Kimble's Beach, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Middle Reed's, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Moore's Beach, Cumberland county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	North Cook's Beach, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	North Reed's Beach, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Nummy Island, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Reed's Beach, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	South Reed's Beach, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Sturgeon Island, Cape May county, NJ	","	NJ	");
INSERT INTO #state_map(district, state) values ("	Villas, NJ	","	NJ	");

INSERT INTO #state_map(district, state) values ("	New Mexico	","	NM	");

INSERT INTO #state_map(district, state) values ("	Albany County,  New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Albany County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Albany County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Albany county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Albany county, NY (Travel to UK)	","	NY	");
INSERT INTO #state_map(district, state) values ("	Albany, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Allegany County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Allegany County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Allegany county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Bennington, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Bronx County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Bronx County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Bronx, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Brooklyn, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Broome  County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Broome County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Broome County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Broome county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cataraugus county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cattaragus county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cattaraugus County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cattaraugus County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cattaraugus county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cayuga County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cayuga county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chautauqua County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chautauqua County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chautauqua county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chemung County,  New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chemung County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chemung county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chenango County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chenango County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Chenango county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Clinton County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Clinton County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Clinton county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Columbia County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Columbia County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Columbia county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cortland County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Cortland county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Delaware County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Delaware county, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Delaware county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Dutchess County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Dutchess County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Dutchess county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Erie County,     NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Erie County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Erie County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Erie county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Essex County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Essex county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Franklin county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Fulton County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Fulton County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Fulton county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Genesee county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Genessee County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Glendale, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Greene County,  NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Greene County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Greene county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Hamilton County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Herkimer County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Herkimer County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Herkimer county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Jamaica Island, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Jefferson County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Jefferson county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Kings County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Kings county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Lewis County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Livingston County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Livingston county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Madison County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Madison County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Madison county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Manhattan county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Monroe County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Monroe County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Monroe county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Montgomery County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Montgomery county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Nassau County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Nassau County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Nassau county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York City	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York City, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York City, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York county, NY (Roosevelt Island)	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York, Ithaca	","	NY	");
INSERT INTO #state_map(district, state) values ("	New York, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Niagara County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Niagara county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Oneida County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Oneida county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Onondaga County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Onondaga County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Onondaga county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Ontario County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Ontario County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Ontario county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Orange County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Orange County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Orleans County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Orleans county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Oswego County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Oswego county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Otsego County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Otsego County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Otsego county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Putnam County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Putnam county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Putnum County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Putnum County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Putnum county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Queens County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Queens County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Queens county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Renesselaer County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Renesselaer county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rensselaer County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rensselaer County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rensselaer county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rensselear County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Richmond county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rockland County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rockland County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Rockland county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Saratoga County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Saratoga County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Saratoga county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Schenectady County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Schenectady County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Schenectady county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Schoharie County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Schoharie County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Schoharie county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Seneca County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Seneca county, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Seneca county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	St. Lawrence County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	St. Lawrence County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	St. Lawrence county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	St.Lawrence county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Steuben County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Steuben County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Steuben county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Suffolk County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Suffolk County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Suffolk county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Sullivan County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Sullivan County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Sullivan county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Tioga County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Tioga county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Tompkins County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Tompkins County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Tompkins county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Ulster County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Ulster County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Ulster county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Warran County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Warren County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Warren county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Washington County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Washington county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Wayne County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Wayne county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Westchester  County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Westchester County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Westchester County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Westchester county, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Wyoming County, New York	","	NY	");
INSERT INTO #state_map(district, state) values ("	Wyoming County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Yates County, NY	","	NY	");
INSERT INTO #state_map(district, state) values ("	Yates county, NY	","	NY	");

INSERT INTO #state_map(district, state) values ("	Akron, OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	Fairfield, OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	Florence, OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	North Canton, OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	Oberlin, OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	Ohio	","	OH	");
INSERT INTO #state_map(district, state) values ("	Toledo, OH	","	OH	");
INSERT INTO #state_map(district, state) values ("	Washington, OH	","	OH	");

INSERT INTO #state_map(district, state) values ("	Choctaw, OK	","	OK	");
INSERT INTO #state_map(district, state) values ("	Norman, OK	","	OK	");
INSERT INTO #state_map(district, state) values ("	Oklahoma	","	OK	");

INSERT INTO #state_map(district, state) values ("	Douglas County, Oregon	","	OR	");
INSERT INTO #state_map(district, state) values ("	Harney County, Oregon	","	OR	");
INSERT INTO #state_map(district, state) values ("	Klamath County, Oregon	","	OR	");
INSERT INTO #state_map(district, state) values ("	Lincoln County, Oregon	","	OR	");
INSERT INTO #state_map(district, state) values ("	Morrow County, Oregon	","	OR	");
INSERT INTO #state_map(district, state) values ("	Newberg, OR	","	OR	");
INSERT INTO #state_map(district, state) values ("	Oregon	","	OR	");

INSERT INTO #state_map(district, state) values ("	Allegheny County, PA	","	PA	");
INSERT INTO #state_map(district, state) values ("	Pennsylvania	","	PA	");
INSERT INTO #state_map(district, state) values ("	Philadelphia	","	PA	");

INSERT INTO #state_map(district, state) values ("	Rhode Island	","	RI	");

INSERT INTO #state_map(district, state) values ("	Beaufort Co., SC	","	SC	");
INSERT INTO #state_map(district, state) values ("	Beaufort County, SC	","	SC	");
INSERT INTO #state_map(district, state) values ("	Beaufort county, SC	","	SC	");
INSERT INTO #state_map(district, state) values ("	Columbia, SC	","	SC	");
INSERT INTO #state_map(district, state) values ("	South Carolina	","	SC	");

INSERT INTO #state_map(district, state) values ("	South Dakota	","	SD	");

INSERT INTO #state_map(district, state) values ("	Dyersburg, TN	","	TN	");
INSERT INTO #state_map(district, state) values ("	Knoxville, TN	","	TN	");
INSERT INTO #state_map(district, state) values ("	Lebanon, TN	","	TN	");
INSERT INTO #state_map(district, state) values ("	Memphis	","	TN	");
INSERT INTO #state_map(district, state) values ("	Memphis, TN	","	TN	");
INSERT INTO #state_map(district, state) values ("	Tennessee	","	TN	");
INSERT INTO #state_map(district, state) values ("	Tullahoma, TN	","	TN	");

INSERT INTO #state_map(district, state) values ("	Baylor, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	Brazoria county, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	Brownsville	","	TX	");
INSERT INTO #state_map(district, state) values ("	Brownsville, Texas	","	TX	");
INSERT INTO #state_map(district, state) values ("	Conroe, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	Eagle Lake, Colorado county, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	Houston	","	TX	");
INSERT INTO #state_map(district, state) values ("	Houston area	","	TX	");
INSERT INTO #state_map(district, state) values ("	Houston, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	San Antonio, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	Temple, TX	","	TX	");
INSERT INTO #state_map(district, state) values ("	Texas	","	TX	");
INSERT INTO #state_map(district, state) values ("	Winnie, Chambers county, TX	","	TX	");

INSERT INTO #state_map(district, state) values ("	Dale City, VA	","	VA	");
INSERT INTO #state_map(district, state) values ("	Richmond, VA	","	VA	");
INSERT INTO #state_map(district, state) values ("	Virginia	","	VA	");
INSERT INTO #state_map(district, state) values ("	Weber City, VA	","	VA	");

INSERT INTO #state_map(district, state) values ("	Bennington, VT	","	VT	");

INSERT INTO #state_map(district, state) values ("	Clallam County, Washington	","	WA	");
INSERT INTO #state_map(district, state) values ("	Clark County, Washington	","	WA	");
INSERT INTO #state_map(district, state) values ("	Grant County, Washington	","	WA	");
INSERT INTO #state_map(district, state) values ("	Gray's harbor County, Washington	","	WA	");
INSERT INTO #state_map(district, state) values ("	King County, Washington	","	WA	");
INSERT INTO #state_map(district, state) values ("	Washington	","	WA	");
INSERT INTO #state_map(district, state) values ("	Woodinville, WA	","	WA	");
INSERT INTO #state_map(district, state) values ("	Yakima County, Washington	","	WA	");

INSERT INTO #state_map(district, state) values ("	SE Wisconsin	","	WI	");
INSERT INTO #state_map(district, state) values ("	Wisconsin	","	WI	");

INSERT INTO #state_map(district, state) values ("	Wyoming	","	WY	");
