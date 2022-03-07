import httpx
import json
import os


ROOT_SITE = "https://mangalib.me/"
SESSION = httpx.Client(http2=True)
COOKIES = {}
#utility functions

def send_request(url):
    global COOKIES
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36"}
    r = SESSION.get(url, headers=headers, timeout=5, cookies=COOKIES)
    COOKIES = r.cookies
    return r

def save_img(url, path_to_save, refer):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
        "Referer": refer,
        "Accept": "image/webp,image/png;q=0.9,image/jpeg,*/*;q=0.8",
        "Cache-Control": "no-store",
        "Host": "img3.cdnlibs.org", #HARDCODED
        "Connection": "Keep-Alive",
        "Accept-Encoding": "gzip"}

    with SESSION.stream("GET", url, headers=headers, cookies=COOKIES) as r:
        with open(path_to_save, 'wb') as f:
            for chunk in r.iter_bytes():
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
        return ROOT_SITE + self.id + "?section=info"

    def gen_chapters_link(self):
        return ROOT_SITE + self.id + "?section=chapters"

    def gen_page_link(self, volume, chapter, page = 1):
        return ROOT_SITE + self.id + "/v" + str(volume) + "/c" + str(chapter) + "?page=" + str(page)

    def get_name(self, response_text):
        f_index = response_text.find("<div class=\"media-name__main\">")
        s_index = response_text.find("</div>", f_index)
        sstr = response_text[f_index + len("<div class=\"media-name__main\">"): s_index]
        return sstr

    def get_orig_name(self, response_text):
        f_index = response_text.find("<div class=\"media-name__alt\">")
        s_index = response_text.find("</div>", f_index)
        sstr = response_text[f_index + len("<div class=\"media-name__alt\">"): s_index]
        return sstr

    def get_volumes(self, response_text):
        f_index = response_text.find("Томов:")
        s_index = response_text.find("Перевод:")
        sstr = response_text[f_index: s_index]
        res = int("".join([i for i in sstr if i.isdigit()]))
        return res

    def get_cover_url(self, response_text):
        f_index = response_text.find("media-sidebar__cover paper")
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
        f_indexes = find_all(response_text, "media-tag-item ")
        res = []
        for f_index in f_indexes:
            f_index = response_text.find(">", f_index) + 1
            s_index = response_text.find("<", f_index + 1)
            res.append(response_text[f_index: s_index])
            print(res[-1])
        return res

    def update_info(self):
        r = send_request(self.gen_info_link())
        response_text = r.text
        with open("site.html", "wb") as site:
            site.write(response_text.encode())
        self.name = self.get_name(response_text)
        self.original_name = self.get_orig_name(response_text)
        self.cover_url = self.get_cover_url(response_text)
        self.description = self.get_description(response_text)
        self.tags = self.get_tags(response_text)

        self.gen_volume_chapters()

        return r.status_code

    def gen_volume_chapters(self):
        req = self.gen_chapters_link()
        r = send_request(req)
        response_text = r.text
        with open("site.html", "wb") as site:
            site.write(response_text.encode())
        f_index = response_text.find("window.__DATA__ = ") + len("window.__DATA__ = ")
        s_index = response_text.find("window._SITE_COLOR_", f_index)
        s_index = response_text.rfind(";", f_index, s_index)
        text = response_text[f_index:s_index]
        data = json.loads(text)
        self.chapters = len(data['chapters']['list'])
        self.volumes = data['chapters']['list'][0]['chapter_volume']

        self.chapters_in_volume = [0] * (self.volumes + 1)
        self.volume_by_chapter = [0] * (self.chapters + 1)
        for i in range(self.volumes + 1):
            self.chapters_in_volume[i] = list()
        for i in data['chapters']['list']:
            self.chapters_in_volume[i['chapter_volume']].append(int(float(i['chapter_number'])))
            self.volume_by_chapter[int(float(i['chapter_number']))] = i['chapter_volume']
        return

    def get_chapter_pages_count(self, volume, chapter):
        if volume < 1 or volume > self.volumes:
            return -1
        if not chapter in self.chapters_in_volume[volume]:
            return -1
        r = send_request(self.gen_page_link(volume, chapter))
        response_text = r.text
        with open("site.html", "wb") as site:
            site.write(response_text.encode())
        f_index = response_text.find("window.__pg = ") + len("window.__pg = ")
        s_index = response_text.find(";", f_index)
        text = response_text[f_index: s_index]
        data = json.loads(text)
        return len(data)

    def get_page_link(self, volume, chapter, page):
        if volume < 1 or volume > self.volumes:
            return -1
        if not chapter in self.chapters_in_volume[volume]:
            return -1
        r = send_request(self.gen_page_link(volume, chapter, page))
        response_text = r.text
        with open("site.html", "wb") as site:
            site.write(response_text.encode())

        f_index = response_text.find("window.__info = ") + len("window.__info = ")
        s_index = response_text.find(";", f_index)
        text = response_text[f_index: s_index]
        info_data = json.loads(text)
        #info_data["img"]["server"]
        srv = info_data["servers"]["main"] + info_data["img"]["url"]

        f_index = response_text.find("window.__pg = ") + len("window.__pg = ")
        s_index = response_text.find(";", f_index)
        text = response_text[f_index: s_index]
        pg_data = json.loads(text)

        return srv + pg_data[page]["u"]



if __name__ == '__main__':
    klinok = MangaAccess("kimetsu-no-yaiba")
    klinok.update_info()
    if not os.path.exists("klinok"):
        os.mkdir("klinok")
    # smart way to save image in correct format
    save_img(klinok.cover_url, "klinok/cover." + klinok.cover_url.split(".")[-1], "")
    count = klinok.get_chapter_pages_count(1, 1)
    if not os.path.exists("klinok/vol1"):
        os.mkdir("klinok/vol1")
    if not os.path.exists("klinok/vol1/1"):
        os.mkdir("klinok/vol1/1")
    for i in range(count):
        lnk = klinok.get_page_link(1, 1, i)
        print(i, "/", count, lnk)
        save_img(lnk, "klinok/vol1/1/pg" + str(i) + ".jpg", klinok.gen_page_link(1, 1, i))
