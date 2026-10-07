# Downloads the 5 Lucene jars this workshop needs into .\lib  (Windows PowerShell)
$V = "10.5.0"
New-Item -ItemType Directory -Force -Path lib | Out-Null
foreach ($a in "core","analysis-common","queryparser","queries","sandbox") {
  Write-Host "Downloading lucene-$a-$V.jar"
  Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/apache/lucene/lucene-$a/$V/lucene-$a-$V.jar" `
                    -OutFile "lib/lucene-$a-$V.jar"
}
Write-Host 'Done. Now run:  java -cp "lib/*" src/Workshop.java "search engine"'
