# teamcity-telegram-plugin
TeamCity notifications for Telegram messenger

## Installation
To install plugin [download zip archive](https://github.com/dancing-elf/teamcity-telegram-plugin/releases) it and copy it to Teamcity \<data directory\>/plugins (it is $HOME/.BuildServer/plugins under Linux and C:\ProgramData\JetBrains\TeamCity\plugins under Windows).
For more information, take a look at [official documentation](https://confluence.jetbrains.com/display/TCD10/Installing+Additional+Plugins)

## Configuration
Plugin adds new section "Telegram Notifier" under profile and administration pages.
### Administration page
Enter bot token. What it is? To use telegram plugin you should create telegram bot.
It sends messages to subscribers.
Firstly find **BotFather** (case sensitive) bot. Then send **/newbot**
command. Enter name and username of new bot. After that, response should contains
line like 110201543:AAHdqTcvCH1vGWJxfSeofSAs0K5PALDsaw. It's bot token.
More information about creation of bots you can find in [Telegram documentation](https://core.telegram.org/bots#creating-a-new-bot).
![admin page](/images/admin_page.png?raw=true)
### Profile page
Telegram bot can't send messages by username or to user who never write to it.
So when Teamcity is running send **any** message to bot. Response should
contains you chat id. Add that chat id under profile page.
![admin page](/images/profile_page.png?raw=true)

## TODO
1. This sources contains freemarker templates from Teamcity Jabber plugin.
   Maybe it violates license. Anybody can view and edit this resources.
   And this is officially supported by Jetbrains. So I hope there is no 
   problem. But in any case I hope I will be able to know it soon.
   Of course plugin can simply use same templates from jabber config 
   directory directly, but it's strange.
2. Maybe some users need proxy to connect to Telegram server from Teamcity
   machine. Right now proxy is not supported :( Please write an issue if
   you need that.
