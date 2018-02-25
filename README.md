# TekkitReference
![Preview](sample.png)

A manual plugin made for Tekkit Classic (3.1.2). Retrieves item information from a MySQL database.

Please report any bugs and issues [here](../../issues/).  
You can download the JAR [here](../../releases/).
## Usage
**General commands**  
*Permission: tekkitreference.ref*
- `/ref <item name> [filter]`  
- `/ref <item id> [filter]`  
- `/ref look`  
- `/ref hand`  

**Administrator commands**  
*Permission: tekkitreference.admin*
- `/ref admin reload`  
- `/ref admin info`

**Availible filters**
- `all`

## Setup
*Note: by default, TekkitReference uses the Public Database. You can change this in the config file.*

1. Download JAR and move it to your `plugins` folder.  
2. Reload/restart/start your server once to create the config file.  

**Optional: hosting your own database:**  

3. Setup a MySQL server locally and import the database from the .sql file.  
4. Modify the config.yml to match your MySQL server's settings.  
5. Reload/restart your server. Check your console if the SQL connection succeeded.

## Changelog
**v1.2.1**
- Blacklisted items which Bukkit can't retrieve the item id from.

**v1.2.0**
- Fixed search for items without data (ex. 151).
- Created seperate functionn for connecting to DB.
- Admin reload command now forces reconnect to DB.
- Replaces *null* with *N/A* in output.

**v1.1.0**
- Removed JFX toolkit and Services, plugin now works for CLI-only machines.
- Added Item ID as search term.

**v1.0.0**  
- Initial version.

## TODO
- ~~Item ID as search term~~
- Add more items to the database