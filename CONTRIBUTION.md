# Contribution Guidelines

## Introduction
Dire's Building Gadgets has 2 primary goals that have been defined by Direwolf20 we always try to adhere to. The goals are to:
1) To be a fun mod for players to use to make building large structures easier. 
2) To be a learning guide for beginner programmers wanting to learn how to code, and start making a Minecraft mod.

These goals should always be thought about when considering a new implementation, a structural code change or any other changes you may thing to add.

Below we have outlined our `guidelines` for interacting with the `Mods` code base and how you should go about creating PR's.

Bear in mind that these are only guidelines and don't necessarily apply in each and every case, but if you choose not to follow a specific Part of this specification, you must be able to reason out why, if asked to do so.

## PR structure
### General Notes
In general an ideal PR should always explain what it does, how it does this and how it was tested. It should also follow the [Code Guidelines](#code-guidelines).
* If you are not yet finished implementing the changes then you should mark the PR as a `draft` on Github
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
- In general your code should be well-structured and easy to read. we'd like to be the first to acknowledge that our code base isn't perfect and in some areas it's really unreadable! We are trying to improve upon the code-base. 
- Your PR should not contain any formatting changes to unchanged code blocks, unless it is directly related to code cleanup. This makes reviewing them unnecessarily complex.

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
    2. `java.` & `javax.`
    3. All static imports
* Use `*` wildcard when more than 3 classes are imported from the same package, except when doing this would create a naming conflict

### Code Style
In general apply common sense to the Code you write. "Effective Java" is also always a good start.
* Avoid unnecessary `else` clauses.
* Avoid large case statements as it's likely not needed. If you find yourself requiring a large case statement block then try to think about how you might be able to refactor your code in a way that avoids the requirement. Small case statements are fine.
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
