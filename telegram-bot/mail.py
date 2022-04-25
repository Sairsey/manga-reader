import email
import email.parser
import email.policy
import base64

# Utility method to decode headers
def decode_header(header):
    decoded_bytes, charset = email.header.decode_header(header)[0]
    if charset is None:
        return str(decoded_bytes)
    else:
        return decoded_bytes.decode(charset)

# Utility method to remove tags
def remove_tags(text):
    s = str()
    is_tag = False
    for i in text:
        if i == '<':
            is_tag = True
        elif i == '>':
            is_tag = False
            s = s + '\n'
        elif not is_tag:
            s = s + i
            
    return s

# Class that represents one email
class mail:

    # Constructor
    def __init__(self, raw_mail_lines):
       raw_email  = b'\n'.join(raw_mail_lines)
       parsed_email = email.message_from_bytes(raw_email)

       self.subject = decode_header(parsed_email['Subject'])
       self.sender = parsed_email['From']
       f = self.sender.find('<') + 1
       if f != -1:
           self.sender = self.sender[f:len(self.sender) - 1]
       self.date = parsed_email['Date']
       self.text = None

       for part in parsed_email.walk():
           if part.is_multipart():
               continue
           elif part.get_content_maintype() == 'text':
               self.text = str(part.get_payload(decode = True).decode(part.get_content_charset()))
       self.text = remove_tags(self.text)

    # Function to create beautiful report
    def create_report(self):
        mail_str = '<b>Subject: </b>%s\n' % self.subject
        mail_str += '<b>From: </b>%s\n' % self.sender
        mail_str += '<b>Date: </b>%s\n' % self.date
        if self.text:
            f = self.text.find('------------------ Stack trace ------------------')
            if f == -1:
                 mail_str += '<b>Text: </b><pre language="c++">%s</pre>\n' % self.text
                 return mail_str
            mail_str += '<b>Text: </b><pre language="c++">%s</pre>\n' % self.text[:f]
            mail_str += '<pre language="c++">%s</pre>\n' % self.text[f:len(self.text)]
        return mail_str
