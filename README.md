# BoardGameCollector
Android app for collecting data about board games. Academic project for the *Ubiquitous computing* course.
* Written in Kotlin.
* Developed in Android Studio.
* Based on the *Empty activity* templates.
* Requires Android >= 8.0 (API >= 26).
* Utilizes the <a href="https://boardgamegeek.com/wiki/page/BGG_XML_API2" title="BoardGameGeek">XMLAPI2</a> from <a href="https://boardgamegeek.com/" title="BoardGameGeek">BoardGameGeek</a>.
* SQLite database designed using <a href="https://sqlitestudio.pl/" title="SQLiteStudio">SQLiteStudio</a>.

## Implemented features
* *MainActivity* - listing all board games in the collection. Options menu allows to: sort the list by name, year or rank; hide expansions; add a new board game manually; add a new board game from BGG; load current BGG ranking for all board games in collection (apart from expansions which are not ranked); reset collection.
* *DetailsActivity* - showing the details of a board game selected in the *MainActivity*. Options menu allows to: go to the *EditActivity*; delete the board game. Clicking on the BGG rank allows to go to the *RankHistoryActivity*.
* *EditActivity* - editing editable details of an existing board game or adding a new board game manually or adding a new board game from Board Game Geek.
* *BGGActivity* - searching board games at Board Game Geek by game name or by username (all games in user's collection). Options menu: adding all search results to collection.
* *RankHistoryActivity* - displaying the rank history of a board game. A button allows to update the rank.
* *LocationsActivity* - displaying all locations with board games located in them; editing a location on click; deleting an empty location on long click; adding new locations.
* Available in both English and Polish (depending on the system language).

## Screenshots
![Screenshots](https://github.com/adam-handke/BoardGameCollector/blob/main/screenshots.jpg?raw=true)

___
Icon made by <a href="https://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a>
