import os
import requests_html

ROOT_SITE = "https://readmanga.io/"
SESSION = requests_html.HTMLSession()

#utility functions

def send_request(url, fast = True):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"}

    r = SESSION.get(url, headers=headers)
    if not fast:
        r.html.render()
    return r

def save_img(url, path_to_save):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"}

    r = SESSION.get(url, headers=headers, stream=True)
    if r.status_code == 200:
        with open(path_to_save, 'wb') as f:
            for chunk in r:
                f.write(chunk)

def find_all(a_str, sub):
    start = 0
    while True:
        start = a_str.find(sub, start)
        if start == -1: return
        yield start
        start += len(sub) # use start += 1 to find overlapping matches

class MangaAccess:
    id = ""
    volumes = 0
    chapters = 0
    chapters_start_link = []
    chapters_in_volume = []
    volume_by_chapter = []
    name = ""
    original_name = ""
    cover_url = ""
    description = ""
    tags = []

    def __init__(self, id):
        self.id = id

    def gen_info_link(self):
        return ROOT_SITE + self.id + "/"

    def gen_page_link(self, volume, chapter, page = 0):
        return ROOT_SITE + self.id + "/vol" + str(volume) + "/" + str(chapter) + "#page=" + str(page)

    def get_name(self, response_text):
        f_index = response_text.find("<span class='name'>")
        s_index = response_text.find("</span>", f_index)
        sstr = response_text[f_index + len("<span class='name'>"): s_index]
        return sstr

    def get_orig_name(self, response_text):
        f_index = response_text.find("<span class='original-name'>")
        s_index = response_text.find("</span>", f_index)
        sstr = response_text[f_index + len("<span class='original-name'>"): s_index]
        return sstr

    def get_volumes(self, response_text):
        f_index = response_text.find("Томов:")
        s_index = response_text.find("Перевод:")
        sstr = response_text[f_index: s_index]
        res = int("".join([i for i in sstr if i.isdigit()]))
        return res

    def get_cover_url(self, response_text):
        f_index = response_text.find("fotorama")
        f_index = response_text.find("src=", f_index) + len("src=") + 1
        s_index = response_text.find("\"", f_index + 1)
        sstr = response_text[f_index: s_index]
        return sstr

    def get_description(self, response_text):
        f_index = response_text.find("itemprop=\"description\"")
        f_index = response_text.find("content=", f_index) + len("content=") + 1
        s_index = response_text.find("\"", f_index + 1)
        sstr = response_text[f_index: s_index]
        return sstr

    def get_tags(self, response_text):
        f_indexes = find_all(response_text, "class=\"elem_tag")
        res = []
        for f_index in f_indexes:
            f_index = response_text.find(">", f_index) + 1
            f_index = response_text.find(">", f_index + 1) + 1
            s_index = response_text.find("<", f_index + 1)
            res.append(response_text[f_index: s_index])
            print(res[-1])
        return res

    def update_info(self):
        r = send_request(self.gen_info_link())
        response_text = r.html.html
        self.volumes = self.get_volumes(response_text)
        self.name = self.get_name(response_text)
        self.original_name = self.get_orig_name(response_text)
        self.cover_url = self.get_cover_url(response_text)
        self.description = self.get_description(response_text)
        self.tags = self.get_tags(response_text)

        self.gen_volume_chapters()

        return r.status_code

    def gen_volume_chapters(self):
        r = send_request(self.gen_page_link(1, 1))
        response_text = r.html.html
        with open("site.html", "wb") as site:
            site.write(response_text.encode())
        f_index = response_text.find("chapterSelectorSelect")
        s_index = response_text.find("</select>", f_index)
        f_indexes = find_all(response_text[f_index:s_index], "<option")
        res = []
        for f in f_indexes:
            f = response_text[f_index:s_index].find("vol", f) + 3
            s = response_text[f_index:s_index].find("\"", f)
            res.append([int(t) for t in response_text[f_index:s_index][f:s].split("/")])

        self.chapters_in_volume = [0] * (self.volumes + 1)
        self.volume_by_chapter = [0] * (len(res) + 1)
        for i in range(self.volumes + 1):
            self.chapters_in_volume[i] = list()
        for i in res:
            self.chapters_in_volume[i[0]].append(i[1])
            self.volume_by_chapter[i[1]] = i[0]
        self.chapters = len(res)
        return

    def get_chapter_pages_count(self, volume, chapter):
        if volume < 1 or volume > self.volumes:
            return -1
        if not chapter in self.chapters_in_volume[volume]:
            return -1
        r = send_request(self.gen_page_link(volume, chapter), False)
        response_text = r.html.html
        with open("site.html", "wb") as site:
            site.write(response_text.encode())
        f_index = response_text.find("<span class=\"pages-count\">") + len("<span class=\"pages-count\">")
        s_index = response_text.find("</span>", f_index)
        res = response_text[f_index: s_index]
        return int(res)

    def get_page_link(self, volume, chapter, page):
        if volume < 1 or volume > self.volumes:
            return -1
        if not chapter in self.chapters_in_volume[volume]:
            return -1
        q = self.gen_page_link(volume, chapter, page)
        r = send_request(q, False)
        response_text = r.html.html
        f_index = response_text.find("<img class=\"manga-img_")
        f_index = response_text.find("src=\"", f_index) + len("src=\"")
        s_index = response_text.find("\"", f_index)
        return response_text[f_index: s_index]

if __name__ == '__main__':
    klinok = MangaAccess("klinok__rassekaiuchii_demonov__A5327")
    klinok.update_info()
    if not os.path.exists("klinok"):
        os.mkdir("klinok")
    #smart way to save image in correct format
    save_img(klinok.cover_url, "klinok/cover." + klinok.cover_url.split(".")[-1])
    #for i in range(1, klinok.volumes + 1):
    count = klinok.get_chapter_pages_count(1, 1)
    if not os.path.exists("klinok/vol1"):
        os.mkdir("klinok/vol1")
    if not os.path.exists("klinok/vol1/1"):
        os.mkdir("klinok/vol1/1")
    for i in range(count):
        lnk = klinok.get_page_link(1, 1, i)
        print(i, "/", count, lnk)
        save_img(lnk, "klinok/vol1/1/pg"+ str(i) + ".jpg")
