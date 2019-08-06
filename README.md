# Calendar Add-on for Vaadin 8

Calendar-component is a UI component add-on for Vaadin 8.
 
## Release notes

Moved to BETA stage and testing for stable release.

### Version 2.0
- Fix:      [Issue#32] Resolves the cell scaling issue when a fixed calendar height (or 100%) is used. - _Thanks to datadobi_
- Enhanced: [Issue#27] Added possibility to set first day of week - _Thanks to voltor_
- Change:   [Issue#33] Range selection events are now also triggered on single clicks - _Thanks to danieljsv_
- Change:   Vaadin version 8.8.5

### Version 2.0-BETA4
- Change:   Vaadin version 8.4.0 for the demo
- Fix:      [Issue#17] BasicBackwardHandler and BasicForewardHandler loop cycle fixed  
- Fix:      [Issue#19] Calendar Days respond if the locale has changes now 

### Version 2.0-BETA3
- Fix:      [Issue#11] Calculation of day names fixed
- Fix:      [Issue#13] Fix default caption set from current locale.
- Enhanced: more builder methods added
- Timeblocks now uses LocalDate instead of Date
- minor fixes

### Version 2.0-BETA2
- Fix:      [Issue#1] missing toolips in month view
- Fix:      [Issue#6] SetHeight to 100% gives wrong height
- Fix:      [Issue#9] items are moveable still even with isMoveable()=false, again only in monthly View
- Fix:      [Issue#10] Calendar scroll not work
- Fix:      Get handler registration
- Fix:      Styling issues
- Fix:      Wrong heights with time blocks added (Experimental feature)
- Fix:      Item not clickable in month view
- Enhanced: individual time caption pattern for time added
- Enhanced: new api to set date ranges added
    - withDay, withDayInMonth, withWeek, withWeekInYear, withMonth, withMonthInYear
- Enhanced: new controls for switching forward and back in month view.
- Enhanced: extended styling of first day in month

### Version 2.0-BETA1
- Change:   Java8 Date/Time-API
- Change:   parsing of dates replaced by object states
- Enhanced: weekly caption provider added
- Change:   deprecated code removed

### Version 1.0
- Fixed:    cell selection style is not added to calendar items anymore 
- Enhanced: convenience methods for time blocks added

### Version 1.0-BETA3
- Fixed:    cell selection style is not added to calendar items anymore 
- Enhanced: convenience methods for time blocks added

### Version 1.0-BETA2
- Fixed:    forward and backward day calculation
- Enhanced: BasicDateClickHandler has a new property to exclude month from cycle
- Enhanced: add blocked timeslots all over or per day. Blocked slots can be styled.
- Enhanced: input states per item added. (CalendarItem.isResizeable | .isMoveable | .isClickable)

### Version 1.0-BETA1
- JS-Errors fixed on moving items
- Naming of events changed to items
- Refactorings
- custom styles in demo-app

### Version 1.0-ALPHA3
- EventProvider generics
- respect descriptionContentMode from state
- getter/setter for ContentMode

### Version 1.0-ALPHA2
- tooltips fixed
- deprecated API partial removed
- getters for daily mode and weekly mode added
- BasicDateClickHandler now cycles between daily, weekly and monthly mode by default
- more interactive demo added

### Version 1.0-ALPHA1
- extracted calendar component from current v7 compatibility package
- container based item providers removed

## Roadmap

This component is developed as a hobby with no public roadmap or any guarantees of upcoming releases. That said, the following features are planned for upcoming releases:
- move over to native Vaadin 8 and Java 8 DateTime-API 

## Download release

Official releases of this add-on are available at Vaadin Directory. For Maven instructions, download and reviews, go to https://vaadin.com/directory#!addon/calendar-add-on

## Building and running demo

git clone <url of the MyComponent repository>
mvn clean install
cd demo
mvn jetty:run

To see the demo, navigate to http://localhost:8080/

### Debugging server-side

If you have not already compiled the widgetset, do it now by running vaadin:install Maven target for calendar-component-root project.

If you have a JRebel license, it makes on the fly code changes faster. Just add JRebel nature to your calendar-component-demo project by clicking project with right mouse button and choosing JRebel > Add JRebel Nature

To debug project and make code modifications on the fly in the server-side, right-click the calendar-component-demo project and choose Debug As > Debug on Server. Navigate to http://localhost:8080/calendar-component-demo/ to see the application.

### Debugging client-side

Debugging client side code in the calendar-component-demo project:
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application or by adding ?superdevmode to the URL
  - You can access Java-sources and set breakpoints inside Chrome if you enable source maps from inspector settings.

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:
- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

The original source of calendar is written by Vaadin Ltd.

Fixes and additions by: 
Lutz Güttler
