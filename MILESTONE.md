Please push the current working version of the codebase to GitHub with the commit message "2.59 completed the logic review and applied fixes to avoid data conflicts.". Run the standard git commands (add, commit, push) to secure this backup.


logs page:
 		make month calendar can collapse to easy for user see logs created below the month calendar.
 		make week calendar can slide to show prev week,next week

#2.34
##make supplies auto detect from product name
	supplies page:
		stock & safety>active ingredient. hint change from e.g. neem oil to azoxystrobin

		0wp", field unit know its g or kg. same goes if user write product name liquid(sc,sc,etc) unit auto become mL or L. what i mean is from product name formulation app can know its liquid or weight. form/type, form. code also auto detect from product name

		intruction/notes. hint change to e.g. "2mL/L or 5g/10L"

		optional fields. remove premade +re-entry interval(rei),dilution rate

#2.35
	assets page:
		location & taxonomy label rename to location & tags
		asset notes. hint rename to remark for asset or asset detail
		optional field premade add "source"

#2.36
	assets page
		make category displayed as 2x2

#2.37
	assets page
		optional fields copy style from logs page optional field. asking field type(text,data,single select,multi select,bolean). optional field created will bound to category type selected. for example user select cateogry type "tree" then he make custom optional field name "nama lain" as multi select" then whenever he want add new tree, the optional field "nama lain" will be there. 

#2.38
	assets page
		assets page>optional field. rename field name hint to "Field Name". so no longer word. 
		when user tap data type, please configure it according the exact data type. for example number will make field number for example -0.5 or 3.00
		remove single select. i think it just same with text data type
		boolean change to radio. so user can make list for example tree1, tree2,tree3 then when filling for user can tap tree3 for example

#2.39
	logs page
		assets page>optional field is perfect. make logs page also follow optional field from assets page

#2.40
	supplies page
		apply optional field style we made earlier to supplies page also

#2.41
	add diy category in supplies page and its fields

#2.42
	supplies page
		add optional field datetime,date, time 
	
#2.43
	supplies page
		auto fill form using product name. for example user write linotyl 80wp or linotyl 80 wp so planfora know it is form/type is powder and form.code is WP

###MEGA CHANGES
#2.44
	logs page
		reconstruct logs pages,add logs id on logs page

#2.45
	logs page
		reconstruct display of logs entry. when user tap view by list then when user tap parent logs, it view child of logs(if exist)

#2.46
	logs page
		on create new activity log page, show all activity type but when user tap activity type, activity type is uncollapse and show selected activity type only. 

#2.47
	logs page
		when user tap filter icon. app will show 2 row
		1-filter by location
		2-below it filter by activity type

#2.48
	logs page
		add search box function into logs page. icon magnifying glass before icon filter.

#2.49
	assets page
		make view asset list copy 3 hierarchy style of logs list page. manage asset per location,group,row

#2.50
	assets page
		make collapsible block/zone,calculate total plant. persistent field asking for total plant in +asset form. diplay block/zone at asset card.

#2.51
	assets page
		reconstruct add new asset field placement,ux

#2.52
	supplies page
		reconstruct add new supplies field ui, introduce sub tab

#2.53
	supplies page
		reconstruct diy sub tab field

#2.54
	supplies page>DIY Lab
		polish. hold tap on created diy card to update progress. update page will open and field is using existing data from creation/created earlier. update only have field Vessel/Jar ID, Material List(with +Add item),location, stock & safety(stock amount, unit), instruction & notes, camera, gallery,audio,metric(+optional field)

#2.55
	add date time to diy lab create, update

#2.56
	supplies>diy lab
remove target benefit field.
move position "location","tags" above "maturity" and below diy specification.
put camera, gallery,audio inside container(like other field) name it attachments

#2.57
	supplies pages. add draft function for creating entry

#2.58
	assets page. add draft function for creating entry

#2.59
	completed the logic review and applied fixes to avoid data conflicts.