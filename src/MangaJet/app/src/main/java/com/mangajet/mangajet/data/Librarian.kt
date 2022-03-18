package com.mangajet.mangajet.data

//Singleton class that stores all libraries with manga and provides access to them
object Librarian {

    //enum class for
    enum class LibraryName{
        Readmanga,
        Mangalib,
        Mangachan,
        Acomics
    }
    //map for storing libraries names as keys and abstract libraries as value
    private val map = hashMapOf<LibraryName, AbstractLibrary?>()

    //something like constructor
    init {
        map[LibraryName.Readmanga] = null
        map[LibraryName.Mangalib] = null
        map[LibraryName.Mangachan] = null
        map[LibraryName.Acomics] = null
    }

    //function to get abstractLibrary from map by key(enum)
    public fun getLibrary(name: LibraryName): AbstractLibrary? {
        return map[name]
    }

}
