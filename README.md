# MAL (the Meta Attack Language)

## Requirements

Install modified maven-jlink-plugin:
```
git clone https://github.com/mal-lang/maven-jlink-plugin.git
cd maven-jlink-plugin
mvn install -Dmaven.test.skip=true
```

Install modified javapoet:
```
git clone https://github.com/mal-lang/javapoet.git
cd javapoet
mvn install -Dmaven.test.skip=true
```

Install modified svgSalamander:
```
git clone https://github.com/mal-lang/svgSalamander.git
cd svgSalamander/svg-core
mvn install -Dmaven.test.skip=true
```

## Compile

Compile MAL with:
```
cd malcompiler
mvn install
```

## Install

Install MAL with:
```
mkdir -p ~/bin/malcompiler
cd ~/bin/malcompiler
unzip /path/to/malcomplier/malcompiler.jlink/target/malcompiler.jlink-0.1.0-SNAPSHOT.zip
echo 'export PATH="$PATH:$HOME/bin/malcompiler/bin"' >> ~/.bashrc
source ~/.bashrc
```

## Run

Run MAL with:
```
malc FILE
```
