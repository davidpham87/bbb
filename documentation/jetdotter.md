# Jetdotter: CLI to transform between JSON, EDN, YAML, and Transit

## Why jetdotter?

I made this quick babashka script in response to my inability to transform
[jet](https://github.com/borkdude/jet) to support YAML. The `dotter` suffix is
usually means *daughter of* in Scandinavian culture, and `jetson` was
already taken by another project.

# Installation

Download the `jetdotter` file in the target folder and place it somewhere on
your `$PATH`.

## Usage

``` shell
# find all json files in current directory and convert them to yaml
ls *.json | xargs bb target/jetdotter --to yaml

# find all edn files and convert them to transit
ls *.edn | xargs bb target/jetdotter --to transit
```

# Caveat

Since transit files are stored as json, their filename extension is
`transit.json` instead of `json`.



## Compile

``` shell
make bb-script # see target/jetdotter
```
