# HunterCraft Remote Blacklist

The server downloads a remote JSON blacklist on startup and kicks matching UUIDs when they join.

Put the people to ban in the remote GitHub JSON file, then point the mod at that file's raw URL in `run/config/huntercraft-common.toml`:

```toml
[blacklist]
url = "https://raw.githubusercontent.com/YOUR_GITHUB_USERNAME/huntercraft-blacklist/main/blacklist.json"
```

Your GitHub `blacklist.json` should use this format:

```json
{
  "blacklist": [
    {
      "uuid": "00000000-0000-0000-0000-000000000000",
      "reason": "Reason shown on the kick screen"
    }
  ]
}
```

To add another banned player, add another object to the `blacklist` array:

```json
{
  "blacklist": [
    {
      "uuid": "00000000-0000-0000-0000-000000000000",
      "reason": "First banned player"
    },
    {
      "uuid": "11111111-1111-1111-1111-111111111111",
      "reason": "Second banned player"
    }
  ]
}
```

Admins can reload it without restarting:

```text
/hunter blacklist reload
```

They can also check an online player:

```text
/hunter blacklist check <player>
```
