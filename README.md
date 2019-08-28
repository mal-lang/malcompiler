# MAL (the Meta Attack Language)

## Requirements

Install modified javapoet:
```
git clone https://github.com/mal-lang/javapoet.git
cd javapoet
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
tar xf /path/to/malcompiler/malcompiler-jlink/target/malcompiler-0.1.0-SNAPSHOT.tar.gz
echo 'export PATH="$PATH:$HOME/bin/malcompiler/bin"' >> ~/.bashrc
source ~/.bashrc
```

## Run

Run MAL with:
```
malc FILE
```
