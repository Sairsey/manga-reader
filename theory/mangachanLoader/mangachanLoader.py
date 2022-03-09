import requests
from bs4 import BeautifulSoup
import json
import re
import os
import math

#Constant URL to parse
PATH = 'chromedriver.exe'
PAGE = '#page='
HOST = 'https://manga-chan.me'
#URL = 'https://manga-chan.me/manga/106438-my-dress-up-darling.html'
URL = 'https://manga-chan.me/manga/27940-wind-breaker.html'
HEADERS = {'user-agent' : 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.19582', 'accept' : '*/*'}
SUCCES_CODE = 200

#class that represent one manga
class manga:
    #data
    manga_original_name = ""
    manga_russian_name = ""
    number_of_volumes = 0
    number_of_chapters = 0
    url = ""
    photo_url = ""

    #constructor
    def __init__(self, url):
        self.url = url

    def fill_data(self):
        html = get_html(self.url)
        if html.status_code != SUCCES_CODE:
            return False
        html.encoding = 'utf-8'
        soup = BeautifulSoup(html.text, 'html.parser')
        #get names
        full_name = soup.find('div', class_ = 'name_row').text
        russian_name = re.findall('\(.*?\)', full_name)[-1]
        if len(russian_name) != 0:
            full_name = full_name.replace(russian_name, '', 1)
            self.manga_russian_name = russian_name.replace(russian_name[-1] + russian_name[0], '')
        self.manga_name = full_name.strip()
        #get photo url
        self.photo_url = soup.find('div', {'id': 'manga_images'}).find('img')['src']
        #get number of volumes and chapters
        first_link = soup.find('table', class_ = 'table_cha').find('a').text.split()
        self.number_of_volumes = int(first_link[1])
        self.number_of_chapters = math.ceil(float(first_link[3]))
        return True

#function to send request to get html for parse
def get_html(url, params = None):
    return requests.get(url, headers = HEADERS, params = params)

#function to download chapters of manga by url and number in order
def load_manga(url, number):
    some_title = manga(url)
    if some_title.fill_data() == True:
        html = get_html(some_title.url)
        html.encoding = 'utf-8'
        soup = BeautifulSoup(html.text, 'html.parser')
        tables = soup.find_all('table', class_ = 'table_cha')
        chapters = []
        for table in tables:
            chapters.extend(table.find_all('a'))
        if len(chapters) < number or number <= 0:
            print('Error: incorrect chapter number')
            return
        url_to_needed_chapter = HOST + (chapters[len(chapters) - number])['href']
        #save photo cover
        if not os.path.exists(some_title.manga_name):
            os.mkdir(some_title.manga_name)
        response = requests.get(some_title.photo_url)
        file = open(some_title.manga_name + '\cover.png', 'wb')
        file.write(response.content)
        file.close()

        html = get_html(url_to_needed_chapter, {'page' : 1})
        html.encoding = 'utf-8'
        soup = BeautifulSoup(html.text, 'html.parser')
        scripts = soup.find_all('script')
        my_script = str([script for script in scripts if 'fullimg' in str(script)][0])
        s = my_script[my_script.find('"fullimg":[') : len(my_script)]
        full_imgs = s[s.find('[') + 1 : s.find(']')].split(',')
        #save photos
        if not os.path.exists(some_title.manga_name + '\\' + str(number)):
           os.mkdir(some_title.manga_name + '\\' + str(number))
        for i in range(0, len(full_imgs) - 1):
            cut = full_imgs[i][1:-1]
            print(f'loading page {i + 1}/{len(full_imgs) - 1}')
            response = requests.get(cut)
            file = open(some_title.manga_name + '\\' + str(number) + '\\' + str(i + 1) + '.png', 'wb')
            file.write(response.content)
            file.close()
    else:
        print('Error in parsering manga ')

load_manga(URL, 64)