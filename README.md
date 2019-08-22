# MAL (the Meta Attack Language)

## Requirements

Install latest maven-jlink-plugin:
```
git clone https://github.com/mal-lang/maven-jlink-plugin.git
cd maven-jlink-plugin
mvn install
```

Install modified javapoet:
```
git clone https://github.com/mal-lang/javapoet.git
cd javapoet
mvn install
```

Install modified svgSalamander:
```
git clone https://github.com/mal-lang/svgSalamander.git
cd svgSalamander/svg-core
mvn install
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
mkdir ~/bin/mal
cd ~/bin/mal
unzip /path/to/malcomplier/mal.img/target/mal.img-0.1.0-SNAPSHOT.zip
echo 'export PATH="$PATH:$HOME/bin/mal/bin"' >> ~/.bashrc
source ~/.bashrc
```

## Run

Run MAL with:
```
malc FILE
```
