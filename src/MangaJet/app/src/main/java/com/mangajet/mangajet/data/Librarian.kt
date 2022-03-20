package com.mangajet.mangajet.data

// Singleton class that stores all libraries with manga and provides access to them
object Librarian {

    // Enum class that represents names for parsed sites
    enum class LibraryName(val resource: String){
        Readmanga("https://readmanga.io"),
        Mangalib("https://mangalib.me"),
        Mangachan("https://manga-chan.me"),
        Acomics("https://acomics.ru");

        companion object {
            fun from(findResource: String): LibraryName = LibraryName.values().first { it.resource == findResource}
        }
    }

    // Map for storing libraries names as keys and abstract libraries as value
    private val map = hashMapOf<LibraryName, AbstractLibrary?>()

    // Initializer block
    init {
        map[LibraryName.Readmanga] = null
        map[LibraryName.Mangalib] = null
        map[LibraryName.Mangachan] = MangaChanLibrary()
        map[LibraryName.Acomics] = null
    }

    // Function to get abstractLibrary from map by key(enum)
    public fun getLibrary(name: LibraryName) : AbstractLibrary? {
        return map[name]
    }

}
