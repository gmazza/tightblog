# Flyway Database Migration

TightBlog now uses [Flyway](https://flywaydb.org/) for database schema management with MySQL databases.

## Overview

Flyway automatically manages database schema migrations, ensuring your database schema is always in sync with your application version. When enabled, Flyway will:

1. Check if the database schema exists
2. If not, run all migration scripts to create the schema
3. Track which migrations have been applied in a special table called `flyway_schema_history`
4. On subsequent startups, only run new migrations that haven't been applied yet

## Configuration

### Enabling Flyway

Flyway is **disabled by default** to maintain backward compatibility with Derby (the default in-memory database). To enable Flyway for MySQL deployments:

Add the following to your `application-tbcustom.properties` file:

```properties
spring.flyway.enabled=true
```

### MySQL Configuration Example

For MySQL deployments (e.g., in Docker), your `application-tbcustom.properties` should look like:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tightblog?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver

# Enable Flyway for automatic database migrations
spring.flyway.enabled=true
```

## Migration Scripts

Migration scripts are located in `src/main/resources/db/migration/` and follow Flyway's naming convention:

- `V1__Initial_schema.sql` - Creates the initial database schema

Future database changes should be added as new versioned migration scripts following the pattern `V{version}__{description}.sql`.

## How It Works

1. **First Run**: When the application starts with an empty database, Flyway will:
   - Create the `flyway_schema_history` table
   - Execute `V1__Initial_schema.sql` to create all tables
   - Record the migration in the history table

2. **Subsequent Runs**: On future application starts, Flyway will:
   - Check the `flyway_schema_history` table
   - Skip already-applied migrations
   - Apply any new migration scripts

3. **Existing Databases**: If you have an existing TightBlog database (created manually), Flyway's `baseline-on-migrate=true` setting allows it to adopt the existing schema without re-running the initial migration.

## Docker Deployment

The Docker setup (`docker/web/application-tbcustom.properties`) has been pre-configured with Flyway enabled for the MySQL database container.

## Backward Compatibility

The existing manual database installation process via `InstallerController` continues to work and is still used for Derby databases. Flyway only activates when explicitly enabled in the configuration.

## Troubleshooting

- **Migration Failed**: Check the Flyway logs for details. The application will not start if a migration fails.
- **Schema Out of Sync**: If you manually modified the database, you may need to repair Flyway's schema history table. See [Flyway documentation](https://flywaydb.org/documentation/command/repair) for details.
- **Disabling Flyway**: Set `spring.flyway.enabled=false` in your configuration file.
