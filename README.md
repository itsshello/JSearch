# JSearch - A Java-Based Web Crawler and Search Engine

## Overview
JSearch is a Java-based web crawler and search engine that allows users to crawl the web and search the collected data efficiently. It features multiple configurable options to refine search queries, control crawling parameters, and adjust application settings.

## Prerequisites
- Ensure that Java is installed within the `/JRE` folder. JSearch requires Java to run, and the runtime environment must be present in this directory.
- Spelling accuracy is crucial when performing searches. The search engine does not include a spell-checking mechanism, so queries must be correctly spelled for optimal results.
- JSearch is a **command-line-based** application. **Double-clicking the executable will not initiate any action**; it must be run through a terminal or command prompt.

## Usage
Run the executable using the following syntax:
```
JSearch.exe [FLAG] [OPTION(s)]
```

### Flags:
- `-c, --crawl`          → Crawl the web.
- `-s, --search`         → Search the database.
- `-se, --settings`      → Display or edit settings.
- `-h, --help`           → Display this help message.

### Options:
**Note:** If specific flags such as `-l` (crawl limit) are not specified, JSearch will use default values from the settings file. Similarly, if the `-q` flag is not provided for searching, JSearch will prompt the user to enter a query.

#### Search Options (Used with `-s, --search`):
- `-q, --query`          → Search query (optional; if not set, JSearch will prompt for input).
- `-t, --top`            → Number of top results to display (uses default if not set).

#### Crawl Options (Used with `-c, --crawl`):
- `-l, --limit`          → Crawl limit (uses default if not set).
- `-eg, --engwiki`       → Restrict crawling to English Wikipedia only.
- `-neg, --nengwiki`     → Exclude restriction to English Wikipedia.
- `-sd, --seed`          → Seed URLs (must be comma-separated; uses default if not set).

#### Settings Options (Used with `-se, --settings`):
- `-sh, --show`          → Show settings.
- `-dp, --dbpath`        → Edit path to the database.
- `-lp, --logspath`      → Edit path to the logs.
- `-st, --searchtop`     → Edit the number of top results.
- `-cm, --crawlmax`      → Edit maximum crawl limit.
- `-ew, --engwiki`       → Enable/disable English Wikipedia-only mode (`yes/no`).

## Examples
### Crawling the Web
To crawl the web with a limit of 500 pages:
```
JSearch.exe -c -l 500
```
To crawl only English Wikipedia:
```
JSearch.exe -c -eg
```
If `-l` is not set, JSearch will use the default limit from the settings.

### Searching the Database
To search for "artificial intelligence" and display the top 5 results:
```
JSearch.exe -s -q "artificial intelligence" -t 5
```
If `-q` is not specified, JSearch will prompt the user for input. If `-t` is not set, the default number of top results will be used.

### Editing Settings
To set the database path:
```
JSearch.exe -se -dp "C:\path\to\database"
```
To display current settings:
```
JSearch.exe -se -sh
```

## Installation
1. Download the latest release from the **Releases** section of this repository.
2. Extract the downloaded package to your desired directory.
3. Ensure that Java is installed in the `/JRE` folder.
4. Run `JSearch.exe` via the command line.

## Notes
- JSearch does **not** perform spell-checking on queries. Ensure proper spelling for accurate search results.
- If a required parameter is not specified, JSearch will use the default settings.
- The program requires Java to be installed in the `/JRE` folder. Without it, the application will not run.

## License
JSearch is released under an open-source license. See the LICENSE file for more details.

## Contact
For issues, contributions, or feature requests, please open an issue in this repository.

