# Contribution guidlines

## Preamble
These guidelines are intended as "guidelines" to how an PR To Dire's Buildinggadgets should be structured. 
Bear in mind that these are only guidelines and don't necessarily apply in each and every case, but if you choose not to follow a specific Part of this specification, you must be able to reason out why, if asked to do so.

## PR structure
### General Notes
In General a PR should explain what it does, how it does this and how it was tested. It should also follow the [Code Guidelines](#code-guidelines).
* If you are not yet finished implementing, prepend it with \[WIP]
* A description must be provided before the PR is merged, to provide record of the Performed changes

### Bug Fix PR's
Your PR should include the following at least the following Information:
* A reference to the corresponding issue or a full description of the issue being fixed
* An explanation how this PR solves/attempts to solve the given issue
* If testing was performed, what tests and how extensive they have been

### Feature Request PR's
Your PR should include the following at least the following Information:
* A reference to the corresponding issue or a full description of the feature being implemented
* An explanation how this PR attempts to implement the given feature
* If testing was performed, what tests and how extensive they have been

## Code Guidelines
In General your code should be well-structured and easy to read. 
We know that as of 1.12 this is not the case for great parts of the mod, but we are trying to improve upon the code-base.
Unless your Pr is directly related to code cleaning then your PR should not contain any formatting changes to unchanged code blocks as this simply makes reviewing them unnecessarily complex.

### Formatting
* Use 4 space indent instead of Tabs
* operators should be surrounded by spaces
  * For example `if(statement == true)`
* a `,` comma to separate types should be followed by exactly one space
* type casts should be followed by exactly one space
* `{` should not be prefixed with returns
* Methods should have exactly one free line between them (or their doc blocks)
* Avoid empty lines at the start of class, interface or enum definitions
* Avoid unnecessary lines in empty Methods
    * `public void emptyImpl() {}`
    * Also try and avoid introducing code that may require empty methods
* Method/Class annotations should gain their own lines in front of the definition
* Parameter Annotations should be surrounded by exactly one space
* Comments shoud always start with a single space `// comment`

### Code Style
In general apply common sense to the Code you write. "Effective Java" is also always a good start.
* Avoid unnecessary else clauses
* Avoid large case statements and it's likely not needed. If you find yourself requiring a large case statement block then try to think about how you might be able to refactor your code in a way that avoids the requirement. Small case statements are fine.
* Prefix Interfaces with `I` - like `ITemplate`
* `@SupressWarnings("...")` should be avoided in General
    * if it needs to be applied, limit it to the smallest possible scope (and create a new variable for it if necessary)
    * An Suppression should always have a comment stating why it is valid to suppress this warning here
    * In general only a `@SupressWarning("unchecked")` should ever be necessary - do not suppress deprecations!
* Capitalize comments
* Use multiline comments where applicable, and single-line-comments only for single line comments
* Write Javadoc instead of comments, where possible. 
    * `@implNote` and `@implSpec` are always possible...
    * try to list all unchecked exceptions with `@throws`
    * leave uncommented parameters out (no `@param obj` without description)
* avoid giant Method-Bodies where possible, by splitting into smaller Methods
* Use Guava's `Preconditions` class for Argument validation
* Validate `@NonNull` values with `Objects.requireNonNull` if necessary
* use getters and setters, even in the declaring class
* avoid getters and setters for protected methods
* use protected methods where external classes do not require access or in instances where you have a private var without getters or setters.
* enforce Immutability where possible
    * use ImmutableTypes for immutable values (`ImmutableList`, `ImmutableSet`, `ImmutableMap`, etc.)
    * avoid public properties
    
### Packages
* Don't use packages like there is a limited allowed amount, for the sake of code readability and maintainabilty make as may packages as logically makes sense. Attempt to group as much common functionality as possible within a package. Use our `common.utils` package as an example of functionality grouping. 
* Respect the 3 base packages: `common`, `client` and `api`. If you ever feel that something doesn't fit in these packages then you're likely thinking about it wrong. 
  - `common` is used for everything that isn't `client` specific or part of our `api`.
  - `client` is used for everything that isn't common funcionality and is purely for the client side of the mod.
  - `api` our api package is for all api related functionality, it's rare you should be using this package unless you're specifically working on api specific additions.
* Use sensible and logical naming for your packages and avoid prefixing or sufixing your package names. For example `common.items.gadgets` is good, `common.itemGadgets` is bad as a sub-package should have been used. 
* Always use lower case and respect the official Java guide to creating packages which can be found [here](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html)
