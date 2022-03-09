import requests
from bs4 import BeautifulSoup
import codecs
import json

#Constant URL to parse
HOST = 'https://manga-chan.me'
URL = 'https://manga-chan.me/catalog'
HEADERS = {'user-agent' : 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36', 'accept' : '*/*'}
SUCCES_CODE = 200

#class that represent one manga
class manga:
    #data
    manga_name = ""
    url = ""
    photo_url = ""
    description = ""
    tags = []

    #constructor
    def __init__(self, name, url, photo_url, description, tags):
        self.manga_name = name
        self.url = url
        self.photo_url = photo_url
        self.description = description
        self.tags = tags


#function to send request to get html for parse
def get_html(url, params = None):
    return requests.get(url, headers = HEADERS, params = params)

#find number of last page
def get_pages_count(html):
    soup = BeautifulSoup(html, 'html.parser')
    pagination = soup.find('div', {'id': 'pagination'}).find('span').find_all('a')
    if pagination:
        return int(pagination[-1].get_text())
    return 1

#calculate offset by number of page
def calculate_offset(page_number):
    if page_number <= 1:
        return 0;
    title_per_page = 20
    return title_per_page * page_number - title_per_page

#get something from html
def get_content(html):
    soup = BeautifulSoup(html, 'html.parser')
    big_chunks = soup.find_all('div', {'class': 'content_row'})#get a lot of information about manga
    mangas = []
    #data to get form html
    for chunk in big_chunks:
        mangas.append(
            manga(
                chunk.find(lambda elm: elm.name == "h2" ).text,#get title
                HOST + chunk.find('div', class_ = 'manga_images').find(href = True)['href'],#get url to manga
                chunk.find('div', class_ = 'manga_images').find('img')['src'],#get url to photo
                chunk.find('div', class_ = 'tags').text.strip(),#get description
                chunk.find('div', class_ = 'genre').text.strip().split(', ')#get tags
            )
        )
    return mangas

#write mangas list to csv
def write_to_json(mangas):
    with open('manga_list.json', 'w', encoding = 'utf-8') as file:
        for item in mangas:
            json.dump({'title' : item.manga_name, 'title url' : item.url, 'photo url' : item.photo_url, 'description' : item.description, 'tags' : ','.join(item.tags)}
                ,file, indent = 5, ensure_ascii=False)

#parse all manga from site to json file
def parse():
    html = get_html(URL)
    if html.status_code == SUCCES_CODE:
        html.encoding = 'utf-8'
        pages_count = get_pages_count(html.text)
        print(f'parsing page {1}/{pages_count}')
        mangas = []
        #parse first page
        mangas.extend(get_content(html.text))
        #parse others pages
        for page in range(2, pages_count + 1):
            print(f'parsing page {page}/{pages_count}...')
            html = get_html(URL, params = {'offset' : calculate_offset(page)})
            html.encoding = 'utf-8'
            if html.status_code == SUCCES_CODE:
                mangas.extend(get_content(html.text))
        write_to_json(mangas)
    else:
        print('Error with acces to site')

parse()