from telegram.ext import*
from telegram.constants import MAX_MESSAGE_LENGTH
from telegram.constants import PARSEMODE_HTML
from mailClient import mailClient
import threading
from mail import mail

# Bot token
API_KEY = YOUR BOT TOKEN

# One mutex to rule them all
mutex = threading.Lock()

# Some reports may be to big => need to split into few messages
def handle_large_text(text):
    while text:
        if len(text) < MAX_MESSAGE_LENGTH:
            yield text
            text = None
        else:
            out = text[:MAX_MESSAGE_LENGTH]
            yield out
            text = text.lstrip(out)

# Response on start command
def start_command(update, context):
    update.message.reply_text('Print /help to get help')

# Response on help command
def help_command(update, context):
     help_string = "Manga Jet Bot Commands:\n" \
                "/login didi.doda@somemail.com password - command to log into your mail\n" \
                "/help - command to get some help"
     update.message.reply_text(help_string)

# Response on any message
def handle_message(update, context):
    update.message.reply_text('Are you retarded?\nGo read /help...\nYou need it')

# Response on set command
def login_command(update, context):
    global login, password, inbox_amount

    if len(context.args) != 2:
        update.message.reply_text('Incorrect input for this command')
        return

    mutex.acquire()
    login = context.args[0]
    password = context.args[1]
    print('Got login and password')
    try:
        client = mailClient(login, password)
    except Exception:
        update.message.reply_text("""Sorry, could not log in into your account\nMaybe you don't allow pop protocol in your mail\nOr login or password were incorrect""")
        mutex.release()
        return
    inbox_amount = client.get_mails_count()
    update.message.reply_text("""Authorization was successful\nNumber of mails in your box: """ + str(inbox_amount))
    mutex.release()

    context.job_queue.run_repeating(main_cycle, interval = 10, context = update.message.chat_id)

# Main cycle where we get reports
def main_cycle(context):
    global login, password, inbox_amount

    mutex.acquire()
    try:
        client = mailClient(login, password)
    except Exception:
        context.bot.send_message("""Sorry, there are some problems with logging into your account\n
                                     Please, log in one more time\n""")
        mutex.release()
        return
    new_inbox_amount = client.get_mails_count()
    while new_inbox_amount > inbox_amount:
        inbox_amount = inbox_amount + 1
        email  = client.get_mail_by_index(inbox_amount)
        report = email.create_report()
        for text in handle_large_text(report):
            context.bot.send_message(context.job.context,
                                text=text, parse_mode=PARSEMODE_HTML)
    mutex.release()

# To log errors
def error(update, context):
    print(f"Update {update} caused error {context.error}")

def main():
    # Create eventHandler with bot token
    updater = Updater(API_KEY, use_context = True)
    # Get dispatcher
    dp = updater.dispatcher
    print("Bot started")

    # Add commands
    dp.add_handler(CommandHandler("start", start_command))
    dp.add_handler(CommandHandler("help", help_command))
    dp.add_handler(CommandHandler("login", login_command))
    dp.add_handler(MessageHandler(Filters.text, handle_message))
    dp.add_error_handler(error)

    # Start bot
    updater.start_polling()
    updater.idle()

if __name__ == '__main__':
    main()
