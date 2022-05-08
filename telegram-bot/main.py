import json
from telegram.ext import*
from telegram.constants import MAX_MESSAGE_LENGTH
from telegram.constants import PARSEMODE_HTML
from mailClient import mailClient
import threading

# One mutex to rule them all
bot_mutex = threading.Lock()
subscribers_mutex = threading.Lock()

# check mail interval
check_mail_interval = 10
# when to notify(23 hours, 59 minutes)
boat_life_in_seconds = 86340

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
    update.message.reply_text('Print /help to get help')

# Response on help command
def help_command(update, context):
     help_string = "Manga Jet Bot Commands:\n" \
                "/subscribe - command to subscribe into report mail bot\n" \
                "/unsubscribe - command to unsubscribe from report mail bot\n" \
                "/help - command to get some help"
     update.message.reply_text(help_string)

# Response on any message
def handle_message(update, context):
    update.message.reply_text('Are you retarded?\nGo read /help...\nYou need it')

# Response on unsubscribe command
def unsubscribe_command(update, context):
    global subscribers
    subscribers_mutex.acquire()
    if update.message.chat.id not in subscribers:
        update.message.reply_text("You are even not a subscriber!")
    else:
        subscribers.remove(update.message.chat.id)
        update.message.reply_text("Unsubscribed!")
        print(str(update.message.chat.id) + " has just unsubscribed")
    subscribers_mutex.release()

# Response on subscribe command
def subscribe_command(update, context):
    global subscribers
    subscribers_mutex.acquire()
    if update.message.chat.id in subscribers:
        update.message.reply_text("You have already subscribed!")
        subscribers_mutex.release()
        return
    subscribers.append(update.message.chat.id)
    update.message.reply_text("Subscribed!")
    print("New subscriber: " + str(update.message.chat.id))
    print("All subscribers: " + str(subscribers))
    subscribers_mutex.release()

# Main cycle where we get emails
def bot_main_cycle(context):
    global bot_login, bot_password, bot_inbox_amount, subscribers

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

# Notify about restart
def notify_about_restart(context):
    print("Restart message send")
    text = "In a few minutes I'm going to restart\n"\
           "Don't forget to /subscribe" 
    subscribers_mutex.acquire()
    for subscriber in subscribers:
        context.bot.send_message(chat_id = subscriber,
                                text=text, parse_mode=PARSEMODE_HTML)
    subscribers_mutex.release()

# Function to send texts
def send_to_all_subscribers(context, text):
    print("Mail send to all subscribers")
    subscribers_mutex.acquire()
    for subscriber in subscribers:
        context.bot.send_message(chat_id = subscriber,
                                text=text, parse_mode=PARSEMODE_HTML)
    subscribers_mutex.release()

# Function to send zips
def send_zip(context, fileName):
    if fileName == "":
        return
    file = open(fileName, "rb")
    print("Zip send to all subscribers")
    subscribers_mutex.acquire()
    for subscriber in subscribers:
        context.bot.send_document(subscriber, file)
    subscribers_mutex.release()
    file.close()

# To log errors
def error(update, context):
    print(f"Update {update} caused error {context.error}")

def main():
    # Global variables of bot 
    global bot_login, bot_password, bot_inbox_amount, subscribers
    subscribers = list()

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
    dp.add_handler(CommandHandler("subscribe", subscribe_command))
    dp.add_handler(CommandHandler("unsubscribe", unsubscribe_command))
    dp.add_handler(MessageHandler(Filters.text, handle_message))
    dp.add_error_handler(error)

    # Start bot
    updater.job_queue.run_repeating(bot_main_cycle, interval = check_mail_interval)
    updater.job_queue.run_once(notify_about_restart, when = boat_life_in_seconds)
    updater.start_polling()
    updater.idle()

if __name__ == '__main__':
    main()
