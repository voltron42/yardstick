# Yardstick

Yardstick is an implementation of Gauge testing in Clojure. Yardstick is built to run any valid Gauge ".spec" test files.

## How To Run

Create your own clojure project and import Yardstick. From your code, make the following call.

    (yardstick.core/run ["resources/specs"])

This will run any and all ".spec" tests in the folder "resources/specs" and all subfolders.

## How To Implement

### Taking Steps ...
In Java, to create your own custom specs, you do the following:

    @Step("Enter query <query> in the search box and submit")
    public void enterQuery(String query) throws InterruptedException { ... }

However, in Clojure, we do this instead:

    (defmethod yardstick.core/do-step 
        "Enter query %1s in the search box and submit" 
        [_ query] 
      ... )

### Getting Your Hooks In ...

In Java, Hooks are just annotated methods, just like Steps.

However, in Clojure, to add Hooks to your test run, implement the "Hooks"
protocol and add it to your call to run.

    (yardstick.core/run ["resources/specs"]
        :hooks (reify yardstick.core/Hooks
                 (before-spec [this spec]
                    ...
                  )
                 (after-spec [this spec]
                    ...
                  )
                 (before-scenario [this spec scenario]
                    ...
                  )
                 (after-scenario [this spec scenario]
                    ...
                  )
                 (before-step [this spec scenario step]
                    ...
                  )
                 (after-step [this spec scenario step]
                    ...
                  )))

### See Your Results

## Usage

FIXME

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
