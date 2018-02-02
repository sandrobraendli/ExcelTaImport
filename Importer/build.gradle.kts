
dependencies {
    compile("org.firebirdsql.jdbc:jaybird-jdk17:2.2.10")
    compile(files("lib/jaybird22.dll"))
    compile(files("lib/jaybird22_x64.dll"))
    compile(files("lib/fbembed.dll"))
}
