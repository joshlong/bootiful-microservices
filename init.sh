B=services
mkdir -p $B/{contacts,bookmarks,passports}/src/main/{java/demo,resources}

for x in {contacts,bookmarks,passports}; do
 echo $x
 cp empty-pom.xml $B/$x/pom.xml
done

cp pom.xml $B/pom.xml

cp -r infrastructure/* services
