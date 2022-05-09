import json
import os
import hmtai
import datetime
import time
from telegram.ext import*
from telegram.constants import MAX_MESSAGE_LENGTH
from telegram.constants import PARSEMODE_HTML
from mailClient import mailClient
import threading
import random

# One mutex to rule them all
bot_mutex = threading.Lock()

# check mail interval
check_mail_interval = 60

#pidor time
prev_pidor_time = datetime.date(2007, 1, 1)
today_pidor = ""

#global subsribers and names
global_subscribers = []
global_names = {}


def get_subscribers():
    return global_subscribers.copy()

def save_subscribers(subscribers):
    global global_subscribers
    global_subscribers = subscribers.copy()

def get_names():
    return global_names.copy()

def save_names(names):
    global global_names
    global_names = names.copy()

# Some reports may be to big => need to split into few messages
def handle_large_text(text):
    while text:
        if len(text) < MAX_MESSAGE_LENGTH - 50:
            yield text
            text = None
        else:
            out = text[:MAX_MESSAGE_LENGTH - 50]
            yield out + "</pre>"
            text = '<pre language="c++">' + text.lstrip(out)

# Response on start command
def start_command(update, context):
    names = get_names()
    names[update.message.chat.id] = update.message.from_user.username
    save_names(names)
    update.message.reply_text('Hello ' +  names[update.message.chat.id] + '-senpai!\n' \
        'I am your little sister and I will tell you about all your crushes <3')

# Response on help command
def help_command(update, context):
     help_string = "Oh, Senpai, you want to know what I can?\n" \
                "/subscribe - write this and I will report you about everything from mail bot\n" \
                "/unsubscribe - write this and I will stop telling you about mail bot\n" \
                "/subscribers - write this and I will tell you about other subscribers\n"\
                "/pidor - write this and I will tell you who are the most gay from all subscribers today\n" \
                "/nsfw - write this and I will send you something from my collection\n" \
                "/help - write this and your little sister will help you with anything <3"
     update.message.reply_text(help_string)

# Response on any message
def handle_message(update, context):
    names = get_names()
    names[update.message.chat.id] = update.message.from_user.username
    save_names(names)
    update.message.reply_text('Baka...\nPlease read /help...\n')

# Response on unsubscribe command
def unsubscribe_command(update, context):
    subscribers = get_subscribers()
    if update.message.chat.id not in subscribers:
        update.message.reply_text("Senpai, you are even not a subscriber!")
    else:
        subscribers.remove(update.message.chat.id)
        update.message.reply_text("If you say so...")
        print(str(update.message.chat.id) + " has just unsubscribed")
    save_subscribers(subscribers)
    names = get_names()
    names[update.message.chat.id] = update.message.from_user.username
    save_names(names)

# Response on subscribers command
def subscribers_command(update, context):
    subscribers = get_subscribers()
    names = get_names()
    if update.message.chat.id not in subscribers:
        update.message.reply_text("Senpai, I can show you other subscribers and even more if " +
                                  "you will become my subscriber!\n" +
                                  "Subscribe please!")
        return
    msg = "Senpai, you are only one for me!\nBut here:\n"
    for subscriber in subscribers:
        msg += names[subscriber] + "\n"
    update.message.reply_text(msg[:-1])
    names[update.message.chat.id] = update.message.from_user.username
    save_names(names)

# Response on subscribe command
def subscribe_command(update, context):
    subscribers = get_subscribers()
    names = get_names()
    if update.message.chat.id in subscribers:
        update.message.reply_text("Senpai, you cannot subscribe two times!")
        return
    subscribers.append(update.message.chat.id)
    names[update.message.chat.id] = update.message.from_user.username
    update.message.reply_text("I subscribed you, senpai!")
    print("New subscriber: " + str(update.message.chat.id))
    print("All subscribers: " + str(subscribers))
    save_subscribers(subscribers)
    save_names(names)

# Response on reboot command
def reboot_command(update, context):
    send_to_all_subscribers(context, "Senpai, I feel a little sleepy... Please check on me on other day!\n"+
                                      "I will be very sad if you won't /subscribe...")


# Response on pidor command
def pidor_command(update, context):
    global today_pidor, prev_pidor_time
    today = datetime.date.today()

    if (today - prev_pidor_time).days == 0:
        update.message.reply_text("Senpai, today`s Pidor is " + today_pidor)
        return
    subscribers = get_subscribers()

    if update.message.chat.id not in subscribers:
        update.message.reply_text("Senpai, it is not fair to choose a Pidor without you! Subscribe please!")
        return

    prev_pidor_time = today
    send_to_all_subscribers(context, "Senpai, someone want to know who is Pidor today! Please get ready!")
    time.sleep(1)
    send_to_all_subscribers(context, "Result will be in 3!")
    time.sleep(1)
    send_to_all_subscribers(context, "2!")
    time.sleep(1)
    send_to_all_subscribers(context, "1!")
    pidor_id = random.choice(subscribers)
    today_pidor = get_names()[pidor_id]
    send_to_all_subscribers(context, "Pidor of today is... " + today_pidor)

# Main cycle where we get emails
def bot_main_cycle(context):
    global bot_login, bot_password, bot_inbox_amount

    bot_mutex.acquire()
    try:
        client = mailClient(bot_login, bot_password)
    except Exception:
        print("Could not check mail")
        bot_mutex.release()
        return
    new_inbox_amount = client.get_mails_count()
    while new_inbox_amount > bot_inbox_amount:
        print("Got mail")
        bot_inbox_amount = bot_inbox_amount + 1
        email  = client.get_mail_by_index(bot_inbox_amount)
        report = email.create_report()
        for text in handle_large_text(report):
            send_to_all_subscribers(context, text)
        send_zip(context, email.fileName)
    bot_mutex.release()

def send_nsfw(update, context):
    nsfw_types = [
        "ahegao",
        "ass",
        "bdsm",
        "blowjob",
        "boobjob",
        "creampie",
        "cum",
        "elves",
        "ero",
        "femdom",
        "foot",
        "gangbang",
        "glasses",
        "hentai",
        "incest",
        "lick",
        "masturbation",
        "nsfwMW",
        "nsfwNeko"
        "orgy",
        "panties",
        "public",
        "pussy",
        "tentacles",
        "yuri"
    ]

    subscribers = get_subscribers()
    if update.message.chat.id not in subscribers:
        update.message.reply_text("Senpai, it is not fair to get something from my collection if " +
                                  "you are not a subscriber!\n" +
                                  "Subscribe please!")
        return

    choice = random.choice(nsfw_types)
    link = hmtai.useHM("29", choice)
    update.message.reply_text("Everything for you, Senpai!")
    context.bot.send_photo(
        chat_id = update.message.chat.id,
        photo=link)

# Function to send texts
def send_to_all_subscribers(context, text):
    subscribers = get_subscribers()
    print("Mail send to all subscribers")
    for subscriber in subscribers:
        context.bot.send_message(chat_id = subscriber,
                                text=text, parse_mode=PARSEMODE_HTML)

# Function to send zips
def send_zip(context, fileName):
    if fileName == "":
        return
    print("Zip send to all subscribers")
    subscribers = get_subscribers()
    for subscriber in subscribers:
        file = open(fileName, "rb")
        context.bot.send_document(subscriber, file)
        file.close()

# To log errors
def error(update, context):
    print(f"Update {update} caused error {context.error}")

def main():
    # Global variables of bot 
    global bot_login, bot_password, bot_inbox_amount

    # Get bot token 
    try:
         file = open("token.json", "r") 
         token_json = json.loads(file.read())
         token = token_json["token"]
    except Exception:
        print("Could not find bot's token")
        return

    # Get login and password
    try:
        file = open("loginInfo.json", "r") 
        info = json.loads(file.read())
        bot_login = info["login"]
        bot_password = info["password"]
    except Exception:
        print("Could not find login or password in json")
        return
    print('Got login and password from json')

    try:
        client = mailClient(bot_login, bot_password)
    except Exception:
        print("Error in logging into bot mail")
        return
    bot_inbox_amount = client.get_mails_count()
    print("Got amount of mails in bot mail")

    # Create eventHandler with bot token
    updater = Updater(token, use_context = True)

    # Get dispatcher
    dp = updater.dispatcher
    print("Bot started")

    # Add commands
    dp.add_handler(CommandHandler("start", start_command))
    dp.add_handler(CommandHandler("help", help_command))
    dp.add_handler(CommandHandler("subscribers", subscribers_command))
    dp.add_handler(CommandHandler("subscribe", subscribe_command))
    dp.add_handler(CommandHandler("reboot", reboot_command))
    dp.add_handler(CommandHandler("unsubscribe", unsubscribe_command))
    dp.add_handler(CommandHandler("pidor", pidor_command))
    dp.add_handler(CommandHandler("nsfw", send_nsfw))
    dp.add_handler(MessageHandler(Filters.text, handle_message))
    dp.add_error_handler(error)

    # Start bot
    updater.job_queue.run_repeating(bot_main_cycle, interval = check_mail_interval)
    updater.start_polling()
    updater.idle()

if __name__ == '__main__':
    main()
