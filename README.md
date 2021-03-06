# deps-jupyter

Run jupyter notebook/lab for projects defined using `deps.edn`.

Code is a slightly modified version of [https://github.com/clojupyter/lein-jupyter](https://github.com/clojupyter/lein-jupyter).

## Usage

Define aliases for containing any dependencies required to run your Clojure notebooks. Example:

```clj
{:paths   ["src" "resources"]
 :deps    {org.clojure/clojure {:mvn/version "1.9.0"}}
 :aliases {:jupyter 
            {:extra-deps {provisdom/deps-jupyter {:git/url "git@github.com:Provisdom/deps-jupyter.git"
                                                  :sha "a5f27f14be8c3f473f0694e3e74cda8b190663ee"}}}
           :kernel {:extra-deps {provisdom/veja {:git/url "git@github.com:Provisdom/veja.git"
                                                 :sha "c3a13146fa9cfe489ae4e486788779a1e861a294"}}}
           :install-kernel {:main-opts ["-m" "provisdom.deps-jupyter" "install-kernel" "-A:jupyter:kernel"]}
           :lab {:main-opts  ["-m" "provisdom.deps-jupyter" "lab"]}}}
```
Here, the `:jupyter` alias is separated to prevent repetition, and can be placed in your global
`deps.edn` file for convenience. We've put notebook-specific deps in the `:kernel` alias. If your
notebook has no `:extra-deps`, this alias isn't necessary. To install the kernel, you will run 
something like the the following in you command shell:

```
clj -A:jupyter -m provisdom.deps-jupyter install-kernel -A:jupyter:kernel
```

The `install-kernel` subcommand requires that you specify the options for the `clj` command
which will execute the Clojure kernel server for your project code. The `-A` option is required,
and should correspond to the aliases referencing Provisdom/deps-jupyter and any other `:extra-deps`
for your notebook. Once the kernel is installed, you may start the notebook/lab server with the 
appropriate subcommand:

```
clj -A:jupyter -m provisdom.deps-jupyter lab
```

OR

```
clj -A:jupyter -m provisdom.deps-jupyter notebook
```

The notebook server subcommands only require the alias referencing Provisdom/deps-jupyter, since the
notebook server does not load your project or notebook code.

The example `deps.edn` above defines some convenient aliases with the command-line options:

```
clj -A:jupyter:install-kernel
clj -A:jupyter:lab
```

## License

Copyright © 2018 Provisdom Corp.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
