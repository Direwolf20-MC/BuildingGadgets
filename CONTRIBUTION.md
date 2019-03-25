# Contribution Guidlines

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
Unless your PR is directly related to code cleaning then your PR should not contain any formatting changes to unchanged code blocks as this simply makes reviewing them unnecessarily complex.

### Formatting
* Use 4 space indent instead of Tabs.
* Operators should be surrounded by spaces:
  * For example `if (statement == true)`.
* A `,` comma to separate types should be followed by exactly one space.
* Type casts should be followed by exactly one space.
* `{` should not be wrapped onto a new line.
* Methods should have exactly one free line between them (or their doc blocks).
* Avoid empty lines at the start of class, interface or enum definitions.
* Avoid unnecessary lines in empty Methods:
  * `public void emptyImpl() {}`.
  * Try to avoid introducing code that may require empty methods.
* Method/Class annotations should gain their own lines in front of the definition.
* Parameter Annotations should be surrounded by exactly one space.
* Comments should always start with a single space `// Comment`.
* Import statments should be ordered in sections with exactly 1 empty line between them.
  1. All other unmatched imports, should be in alphebetical order
  2. `java.`, `javax.`, etc.
  3. All static imports
* Use `*` wildcard when more than 3 classes are imported from the same package, except when doing this would create a naming conflict

### Code Style
In general apply common sense to the Code you write. "Effective Java" is also always a good start.
* Avoid unnecessary `else` clauses.
* Avoid large case statements as it's likely not needed. If you find yourself requiring a large case statement block then try to think about how you might be able to refactor your code in a way that avoids the requirement. Small case statements are fine.
  * In most cases this can be avoided by giving the object that's being tested on an abstract method to be overridden. For example, give Item and Block objects a method called `getGuiElement(EntityPlayer, int, int ,int)` and call it in `GuiProxy` instead of using a case statement.
  * The other approach would be using a `Map<SomePropertyOfMyObject, Consumer<MyObject>>` and setup all different cases as lambdas where the parameter is the object being tested.
* Prefix Interfaces with `I` - like `ITemplate`.
* `@SupressWarnings("...")` should be avoided in General:
  * If it needs to be applied, limit it to the smallest possible scope (and create a new variable for it if necessary).
  * An Suppression should always have a comment stating why it is valid to suppress this warning here.
  * In general only a `@SupressWarning("unchecked")` should ever be necessary - do not suppress deprecations!
* Capitalize comments.
* Use multiline comments where applicable, and single-line-comments only for single line comments.
* Write Javadoc instead of comments, where possible:
  * `@implNote` and `@implSpec` are always possible...
  * Try to list all unchecked exceptions with `@throws`
  * Leave uncommented parameters out (no `@param obj` without description)
* Avoid giant Method-Bodies where possible, by splitting into smaller Methods
* Use Guava's `Preconditions` class for Argument validation
* Validate `@NonNull` values with `Objects.requireNonNull` if necessary
* use getters and setters, even in the declaring class
* Use protected fields in non-API superclasses, rather than private ones with getters and setters, if:
  1. Those field are barely accessed in that superclass and are heavily accessed in its subclasses.
  2. There is no additional need for getters and setters, such as:
    1. External access to the field.
    2. The need to run some additional code or logic when getting or setting the value of the field.
* Avoid non-private variables in API-Code and provide getters/setters instead. Bear in mind that this does not apply to constants (`public static final Type xyz = ...`) though.
* In general try to use the smallest visibility-scope possible, expcept if it's likely that others will want access to the given method.
* Enforce immutability where possible
  * Use immutablet types for immutable values (`ImmutableList`, `ImmutableSet`, `ImmutableMap`, etc.)
  * Avoid public properties

### Packages
* Don't use packages like there is a limited allowed amount, for the sake of code readability and maintainabilty make as may packages as logically makes sense. Attempt to group as much common functionality as possible within a package. Use our `common.utils` package as an example of functionality grouping. 
* Respect the 3 base packages: `common`, `client` and `api`. If you ever feel that something doesn't fit in these packages then you're likely thinking about it wrong:
  * `common` is used for everything that isn't `client` specific or part of our `api`.
  * `client` is used for everything that isn't common funcionality and is purely for the client side of the mod.
  * `api` our api package is for all api related functionality, it's rare you should be using this package unless you're specifically working on api specific additions.
* Use sensible and logical naming for your packages and avoid prefixing or sufixing your package names. For example `common.items.gadgets` is good, `common.itemGadgets` is bad as a sub-package should have been used. 
* Always use lower case and respect the official Java guide to creating packages which can be found [here](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html).
