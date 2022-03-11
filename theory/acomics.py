import requests
from bs4 import BeautifulSoup
import re
import json
import os

#Constant URL to parse
HOST = 'https://acomics.ru'
URL = 'https://acomics.ru/comics'
HEADERS = {'user-agent' : 'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36', 'accept' : '*/*'}
SUCCES_CODE = 200

#class that represent one comics
class comics:
    #data
    comics_name = ""
    number_of_pages = 0
    url = ""
    photo_url = ""
    description = ""

    #constructor
    def __init__(self, comics_name, number_of_pages, url, photo_url, description):
        self.comics_name = comics_name
        self.number_of_pages = number_of_pages
        self.url = url
        self.photo_url = photo_url
        self.description = description

#function for debug 
#print all information about manga to json
def write_to_json(comics):
    with open('comics_list.json', 'w', encoding = 'utf-8') as file:
        for item in comics:
            json.dump({'title' : item.comics_name, 'number of pages' : item.number_of_pages, 'title url' : item.url, 'photo url' : item.photo_url, 'description' : item.description}
                ,file, indent = 5, ensure_ascii = False)

#function to send request to get html for parse
def get_html(url, params = None):
    return requests.get(url, headers = HEADERS, params = params)

#function to get comics array from html
def get_content(html):
    soup = BeautifulSoup(html, 'html.parser')
    big_chunks = soup.find_all('table', {'class': 'catalog-elem list-loadable'})#get a lot of information about comics
    comics_titles = []
    #data to get form html
    for chunk in big_chunks:
        comics_titles.append(
            comics(
               chunk.find('div', class_ = 'title').text.strip(),#comics_name
               int(chunk.find('span', class_ = 'total').text.split()[0]),#number_of_pages
               chunk.find('div', class_ = 'title').find(href = True)['href'],#url
               HOST + chunk.find('td', class_ = 'catdata1').find('img')['src'],#photo_url
               chunk.find('div', class_ = 'about').text.strip(),#url#description
            )
        )
    return comics_titles

#function to search titles by name and put result in json file
def get_comics_by_name():
    print("Please, write name of the manga you are looking for")
    title_name = input()
    while len(title_name) < 3:
      print("Minimum length of name is 3 characters!")
      title_name = input()
    html = get_html(HOST + '/search', params = {'keyword' : title_name})
    if html.status_code == SUCCES_CODE:
        html.encoding = 'utf-8'
        return get_content(html.text)
    else:
        print('Error with access to site')
    return []

#function to prettify printing of comics list
def print_list_of_comics(comics_titles):
    for i in range (0, len(comics_titles)):
        print(f'[{i+1}] ' + comics_titles[i].comics_name + 'n')

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

#function to download page from comics by number
def load_comics_page(comics_title, number):
    #save photo cover
    if not os.path.exists(comics_title.comics_name):
        os.mkdir(comics_title.comics_name)
    response = requests.get(comics_title.photo_url)
    file = open(comics_title.comics_name + '\cover.png', 'wb')
    file.write(response.content)
    file.close()

    html = get_html(comics_title.url + '/' + str(number))
    html.encoding = 'utf-8'
    soup = BeautifulSoup(html.text, 'html.parser')
    #save photo
    response = requests.get(HOST + soup.find('div', class_ = 'serial-nomargin').find('img')['src'])
    file = open(comics_title.comics_name + '\\' + str(number) + '.png', 'wb')
    file.write(response.content)
    file.close()

#main
if __name__ == "__main__":
    #find all comics by user input
    comics_titles = get_comics_by_name()
    while len(comics_titles) == 0:
        print('Could not find any comics with that name')
        print('Want to try again?[yes/no]')
        answer = input()
        if answer == 'yes':
            comics_titles = get_comics_by_name()
            continue
        elif answer == 'no':
            exit()
    #write_to_json(comics_titles)
    #ask what comics user want to save
    print_list_of_comics(comics_titles)
    while True:
        print('Choose manga to download')
        comics_index = input()
        if check_input(comics_index, len(comics_titles)) == True:
            break
    comics_to_download = comics_titles[int(comics_index) - 1]
    #ask what photo to download
    print(f'{comics_to_download.comics_name} have {comics_to_download.number_of_pages} pages')
    while True:
        print('Input a number of page to download')
        chapter_number = input()
        if check_input(chapter_number, comics_to_download.number_of_pages) == True:
            break
    #download comics page
    load_comics_page(comics_to_download, int(chapter_number))
    print('page downloaded')