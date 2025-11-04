import "tidal:tidal.ql"

provider "spotify"

collection demo = "96 crayons" collection by "rubbaboy" order[sequential]

song next = demo.nextSong()

provider "tidal"

printf("Song: %s  %s", [next.getTitle(), next.getId()])
