#!/usr/bin/env kscript
//DEPS khttp:khttp:0.1.0,com.offbytwo:docopt:0.6.0.20150202

import org.docopt.Docopt
import khttp.get
import khttp.post

val usage = """
Tag pivotal trackes stories based on your commit messages
Required environment variables: github token, pivotal tracker token, github api url
Usage: tagger.kts [options] <owner> <repo> <project-id-1,project-id-2>
"""

val doArgs = Docopt(usage).parse(args.toList())
println("Parsed script arguments are: \n" + doArgs)

val trackerToken = System.getenv("TRACKER_TOKEN")
val githubToken = System.getenv("GITHUB_TOKEN")
val githubApiUrl = System.getenv("GITHUB_API_URL")
if (trackerToken == null || githubToken == null || githubApiUrl == null) {
    println("Fix the null env var")
    println("TRACKER_TOKEN:$trackerToken GITHUB_TOKEN:$githubToken GITHUB_API_URL:$githubApiUrl")
    System.exit(-1)
}

val owner = args[0]
val repo = args[1]
val trackerIds = args[2].split(',')

val storyIdRegex = Regex(pattern = """\d{6,10}""")
val gitUrl = "https://$githubApiUrl/repos/$owner/$repo/commits?access_token=$githubToken"
val commits = get(gitUrl).jsonArray
var storiesFoundInCommits: ArrayList<String> = ArrayList() 

for (i in 0..(commits.length() - 1)) {
    val commitMessage = commits.getJSONObject(i).getJSONObject("commit").getString("message")
    val matches = storyIdRegex.findAll(input = commitMessage)
    matches.forEach { 
      if (!storiesFoundInCommits.contains(it.value)) {
          storiesFoundInCommits.add(it.value)
      }
    }
}

//todo make sure adding the label works
//todo make sure the label exists before adding it
//todo think about some error handling
for (story in storiesFoundInCommits) {
    for (project in trackerIds) {
        println("tagging tacker $project for changes in $owner $repo")
        val url = "https://pivotaltracker.com/services/v5/projects/$project/stories/$story"
        val payload = mapOf("label" to repo)
        val r = post(url, headers=mapOf("X-TrackerToken" to trackerToken), data=JSONObject(payload))
        val println(r)
    }
}

