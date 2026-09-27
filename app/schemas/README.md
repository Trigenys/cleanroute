# Room schemas

Room schema JSON is generated here by the Room Gradle plugin.

The directory is version-controlled because future database upgrades must be reviewed against the previous schema. Version 1 is generated from the first persisted CleanRoute database model.

Rules:

- never use destructive migration as a default recovery path;
- every database version bump must add an explicit migration or a documented reason why no migration is required;
- migration/reopen tests must pass before a schema change is merged.
