import "tidal:tidal.ql"
import "std:provider.ql"

provider "spotify"

collection demo = "96 crayons" collection by "rubbaboy" order[sequential]

song next = demo.nextSong()
song next2 = demo.nextSong()


printf("Song: %s  %s", [next.getTitle(), next.getId()])
printf("Song: %s  %s", [next2.getTitle(), next2.getId()])

print("Switching to Tidal")

provider "tidal"

setFetchResolveStrategy("fuzzy")

printf("Song: %s  %s", [next.getTitle(), next.getId()])
printf("Song: %s  %s", [next2.getTitle(), next2.getId()])
