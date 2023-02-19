// print all env vars with textbase
System.getenv()
        .sort()
        .findAll { it =~ /(?i)textbas/ }
        .each {println(it)}

println false ? "da" : "nu"