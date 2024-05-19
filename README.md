# PlayerWarps
Advanced warping plugin: private/public warps, adding friends, and limits
* Per player warps.
* Private warps.
* Adding friends to private warps.
* Public warps.
* Permission based limits to set the maximum private warps, public warps and the total warps.
* Language file to change all messages (based on [InteractiveMessenger](https://github.com/NLthijs48/InteractiveMessenger))

## Information
* **Javadocs:** https://wiefferink.me/PlayerWarps/javadocs

### Commands
The base command is `/warp`, which has the following subcommands:
* `/warp to <warp> [player]`: Teleport to a warp by name, optionally from another player.
* `/warp add <name> [private|public] [player]`: Add a warp with the given name, private or public, possibly for another player.
* `/warp del <name> [player]`: Delete a warp by name, possibly for another player.
* `/warp list [player]`: List all warps of a player, public and private ones.
* `/warp info <name> [player]`: Get details about a warp (exact location), possibly for another player.
* `/warp public [player]`: List all public warps on the server, or optionally for only one player.
* `/warp trust <player> <warp> [player]`: Give a player access to a private warp, possibly for another player.
* `/warp untrust <player> <warp> [player]`: Remove access to a private warp for a player, possibly for another player.

**Aliases:** The following aliases are defined to make it easier for player to setup warps:
* `/home`: `/warp to`
* `/sethome`: `/warp add`
* `/delhome`: `/warp del`

### Permissions
```yaml
playerwarps.*:
  description: Give access to all commands of PlayerWarps
  children:
    playerwarps.help: true
    playerwarps.limits.unlimited: true
    playerwarps.limits.default: false
    playerwarps.to: true
    playerwarps.topublic: true
    playerwarps.toprivate: true
    playerwarps.add: true
    playerwarps.addother: true
    playerwarps.del: true
    playerwarps.delother: true
    playerwarps.trust: true
    playerwarps.trustother: true
    playerwarps.untrust: true
    playerwarps.untrustother: true
    playerwarps.list: true
    playerwarps.listother: true
    playerwarps.public: true
    playerwarps.info: true
    playerwarps.infoprivate: true
    playerwarps.infopublic: true
    playerwarps.tabcomplete: true
playerwarps.help:
  description: Allows you to see the help pages
  default: true
playerwarps.limits.default:
  description: Default limits for creating warps
  default: not op
playerwarps.limits.unlimited:
  description: Unlimited for OP's as default
  default: op
playerwarps.to:
  description: Warp to a warp from yourself
  default: true
playerwarps.topublic:
  description: Warp to a public warp from another player
  default: true
playerwarps.toprivate:
  description: Warp to a private warp from another player
  default: op
playerwarps.add:
  description: Add a warp (public and private)
  default: true
playerwarps.addother:
  description: Add a warp for another player
  default: op
playerwarps.del:
  description: Delete a warp from yourself
  default: true
playerwarps.delother:
  description: Delete a warp for another player
  default: op
playerwarps.list:
  description: List your warps
  default: true
playerwarps.listother:
  description: List warps of another player
  default: op
playerwarps.public:
  description: List public warps
  default: true
playerwarps.trust:
  description: Trust a player for a warp from yourself
  default: true
playerwarps.trustother:
  description: Trust a player for a warp from another player
  default: op
playerwarps.untrust:
  description: Untrust a player for a warp from yourself
  default: true
playerwarps.untrustother:
  description: Untrust a player for a warp from another player
  default: op
playerwarps.info:
  description: Display info about warps
  default: true
playerwarps.infoprivate:
  description: Display info about private warps from other players
  default: op
playerwarps.infopublic:
  description: Display info about public warps from other players
  default: true
playerwarps.tabcomplete:
  description: Tab complete commands
  default: true
```
