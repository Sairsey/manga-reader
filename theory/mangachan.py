import requests
from bs4 import BeautifulSoup
import re
import json
import os

#Constant URL to parse
HOST = 'https://manga-chan.me'
URL = 'https://manga-chan.me/catalog'
HEADERS = {'user-agent' : 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36', 'accept' : '*/*'}
SUCCES_CODE = 200

#class that represent one manga
class manga:
    #data
    manga_name = ""
    manga_russian_name = ""
    number_of_volumes = 0
    number_of_chapters = 0
    url = ""
    photo_url = ""
    description = ""
    tags = []

    #constructor
    def __init__(self, name, russian_name, number_of_volumes, number_of_chapters, url, photo_url, description, tags):
        self.manga_name = name
        self.manga_russian_name = russian_name
        self.number_of_volumes = number_of_volumes
        self.number_of_chapters = number_of_chapters
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
    navigation = soup.find('div', class_ = 'navigation')
    if navigation:
        return len(navigation.find_all('a'))
    return 1

#function to get manga array from html
def get_content(html):
    soup = BeautifulSoup(html, 'html.parser')
    big_chunks = soup.find_all('div', {'class': 'content_row'})#get a lot of information about manga
    mangas = []
    #data to get form html
    for chunk in big_chunks:
        #get names
        name = chunk.find(lambda elm: elm.name == "h2" ).text#get title
        russian_name = re.findall('\(.*?\)', name)[-1]
        if len(russian_name) != 0:
            name = name.replace(russian_name, '', 1).strip()
            russian_name = russian_name[1:-1]
        #get volumes and chapters
        volumes_str = chunk.find('div', class_ = 'manga_row3').find('div', class_ = 'item2').text
        chapters_str = chunk.find('div', class_ = 'manga_row3').find('span').text
        volumes_array = volumes_str.replace(chapters_str, '', 1).split()
        volumes_number = 1
        if len(volumes_array) != 1:
            volumes_number = int(volumes_array[0])
        chapters_number = int(chapters_str.split()[0])
        mangas.append(
            manga(
                name,#title name
                russian_name,#russian title name
                volumes_number,#number of volumes
                chapters_number,#number of chapters
                chunk.find(lambda elm: elm.name == "h2" ).find(href = True)['href'],#get url to manga
                chunk.find('div', class_ = 'manga_images').find('img')['src'],#get url to photo
                chunk.find('div', class_ = 'tags').text.strip(),#get description
                chunk.find_all('div', class_ = 'item2')[2].text.strip().split(', ')#get tags
            )
        )
    return mangas

#function to search titles by name
def get_mangas_by_name():
    print("Please, write name of the manga you are looking for")
    title_name = input()
    while len(title_name) < 4:
      print("Minimum length of name is 4 characters!")
      title_name = input()
    html = get_html(URL, params = {'do' : 'search', 'subaction' : 'search', 'story' : title_name})
    if html.status_code == SUCCES_CODE:
        html.encoding = 'utf-8'
        pages_count = get_pages_count(html.text)
        mangas = []
        #parse first page
        mangas.extend(get_content(html.text))
        for page in range(2, pages_count + 1):
            html = get_html(URL, params = {'do' : 'search', 'subaction' : 'search', 'search_start' : page, 'story' : title_name})
            html.encoding = 'utf-8'
            if html.status_code == SUCCES_CODE:
                mangas.extend(get_content(html.text))
        return mangas
    else:
        print('Error with access to site')
    return []

#function for debug 
#print all information about manga to json
def write_to_json(mangas):
    with open('manga_list.json', 'w', encoding = 'utf-8') as file:
        for item in mangas:
            json.dump({'title' : item.manga_name, 'russian name' : item.manga_russian_name, 'number of volumes' : item.number_of_volumes, 'number of chapters' : item.number_of_chapters, 'title url' : item.url, 'photo url' : item.photo_url, 'description' : item.description, 'tags' : ', '.join(item.tags)}
                ,file, indent = 8, ensure_ascii = False)

#function to prettify printing of manga list
def print_list_of_manga(mangas):
    for i in range (0, len(mangas)):
        print(f'[{i+1}] ' + mangas[i].manga_name + 'n')

#function to check that index is correct
def check_input(index, max_index):
    try:
        index = int(index)
    except ValueError:
        print('Error: index must be integer')
        return False
    if index < 1 or index > max_index:
        print('Error: index out of range')
        return False
    return True

#function to download chapters of manga number in order
def load_manga(manga, number):
    html = get_html(manga.url)
    html.encoding = 'utf-8'
    soup = BeautifulSoup(html.text, 'html.parser')
    tables = soup.find_all('table', class_ = 'table_cha')
    chapters = []
    for table in tables:
        chapters.extend(table.find_all('a'))
    url_to_needed_chapter = HOST + (chapters[len(chapters) - number])['href']
    #save photo cover
    if not os.path.exists(manga.manga_name):
        os.mkdir(manga.manga_name)
    response = requests.get(manga.photo_url)
    file = open(manga.manga_name + '\cover.png', 'wb')
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
    if not os.path.exists(manga.manga_name + '\\' + str(number)):
        os.mkdir(manga.manga_name + '\\' + str(number))
    for i in range(0, len(full_imgs) - 1):
        cut = full_imgs[i][1:-1]
        print(f'loading page {i + 1}/{len(full_imgs) - 1}')
        response = requests.get(cut)
        file = open(manga.manga_name + '\\' + str(number) + '\\' + str(i + 1) + '.png', 'wb')
        file.write(response.content)
        file.close()

#main
if __name__ == "__main__":
    #find all manga by user input
    mangas = get_mangas_by_name()
    while len(mangas) == 0:
        print('Could not find any manga with that name')
        print('Want to try again?[yes/no]')
        answer = input()
        if answer == 'yes':
            mangas = get_mangas_by_name()
            continue
        elif answer == 'no':
            exit()
    #write_to_json(mangas)
    #ask what manga user want to save
    print_list_of_manga(mangas)
    while True:
        print('Choose manga to download')
        manga_index = input()
        if check_input(manga_index, len(mangas)) == True:
            break
    manga_to_download = mangas[int(manga_index) - 1]
    #ask what chapter to download
    print(f'{manga_to_download.manga_name} have {manga_to_download.number_of_chapters} chapters')
    while True:
        print('Input a number of chapter to download')
        chapter_number = input()
        if check_input(chapter_number, manga_to_download.number_of_chapters) == True:
            break
    #download manga
    load_manga(manga_to_download, int(chapter_number))