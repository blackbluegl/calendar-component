# Calendar Add-on for Vaadin 8

Calendar-component is a UI component add-on for Vaadin 8.
 
## Release notes

Moved to BETA stage and testing for stable release. After that, a new feature-branch will be opened.  
(see issues -> tag:enhancements)

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
