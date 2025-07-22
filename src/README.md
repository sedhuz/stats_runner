# Stats Runner

**Stats Runner** is a multi-threaded MySQL query executor built in Java. It connects to multiple hosts and databases, executes a provided query across them, logs the outcomes, and aggregates the results into a single CSV file. Ideal for system-wide analytics, auditing, or data extraction across replicated databases.

## Usage

- Enter your query in input/input.sql
- Run StatsRunner.java's `main` method

- To configure, try tweaking the values in `app.properties

- **Outputs**
  - You can find logs with in `logs/` folder
  - And aggregated result in `output/` folder as `.csv`

---

## Info for Nerds

### Project Structure

```
src/
├── db/              # Database API
├── io/              # IO utilities
├── log/             # Logging system
├── properties/      # Property loader (hosts, regex, threads, etc.)
└── StatsRunner.java # Main entry point
```

### 🛠️ Configuration (via `PropertiesAPI`)

Ensure `PropertiesAPI` provides values for:

```properties
# example app.properties

# db
db.username=your_username
db.password=your_password
db.hosts=host1,host2
db.skippeddbsregex=^(mysql|information_schema|performance_schema|sys)$

# app
app.maxthreads=12
app.parallelenabled=true

# log
log.dir=src/main/resources/log
log.datetimeformat=dd-MM-yyyy hh:mm:ss a
log.datetimeformatforfile=dd-MMM-yyyy_hh:mm:ssa

# io
input.file=src/main/resources/input/input.sql
output.dir=src/main/resources/output/
output.datetimeformatforfile=dd-MMM-yyyy_hh:mm:ssa
```

### ⚠️ Notes
- Switch expressions require Java 14+. Set proper --release in IntelliJ or Gradle:
- Settings > Build, Execution, Deployment > Compiler > Java Compiler
- Set Target bytecode version to 17 or 21
- Optimize max.threads based on system load and number of hosts

---

### Author

- Developed by `sedhu.b` – feel free to fork, use, or extend it.
