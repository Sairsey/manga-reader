import poplib
from mail import mail

# Class that represents email box
class mailClient:

    # Constructor
    def __init__(self, login, password):
        self.login = login
        self.password = password
        # Get pop_3 server with specific mail
        pop3_server = 'pop.' + login.split('@')[-1]
        self.server = poplib.POP3_SSL(pop3_server)
        self.server.user(self.login)
        self.server.pass_(self.password)

    # Function to get mails count in box
    def get_mails_count(self):
        _, mails, _ = self.server.list()
        return len(mails)

    # Function to get mail by index
    def get_mail_by_index(self, index):
        _, email, _ = self.server.retr(index)
        return mail(email)
