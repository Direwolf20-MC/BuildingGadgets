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
Also your PR should not contain any formatting only changes to Files, as this simply makes reviewing them unnecessarily complex.
Try to avoid unnecessary commits in order not to pollute the commit history.
### Formatting
* Use 4 space indent instead of Tabs
* operators should be surrounded by spaces
* a `,` comma to separate types should be followed by exactly one Space
* type casts should be followed by exactly one space
* `{` should not be prefixed with returns
* Methods should have exactly one free line between them (/their docs)
* Avoid empty lines at the start of class, interface or enum definitions
* Avoid unnecessary lines in empty Methods
    * `public void emptyImpl() {}`
* Method/Class annotations should gain their own lines in front of the definition
* Parameter Annotations should be surrounded by exactly one space
### Code Style
In general apply common sense to the Code you write. "Effective Java" is also always a good start.
* Avoid unnecessary else clauses
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
* enforce Immutability where possible
    * use ImmutableTypes for immutable values (`ImmutableList`, `ImmutableSet`, `ImmutableMap`, etc.)
    * avoid public properties
