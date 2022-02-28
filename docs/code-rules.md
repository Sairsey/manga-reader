# Code rules
  1) In 3 days before deadline must be test and review.     
  2) No "magic" numbers and constants in code. Everything must be in enums, defines or special variables.   
  3) Use global variables only after team discussion.    
# Code style:    
  1) Every figure "{" must be on same string. 
 ```Kotlin
      fun Foo(a : Int) {
	      ...
      }
 ```
  2) Single if-else must be with " { ... }".     
 ```Kotlin
      fun Foo(a : Int) {
	    if (n > 0) {
         ...
        }
        else {
        ...
        }
      }
 ```
  3) Naming.
  *	camelCase for: local variables, class members
  *	PascalCase for: functions
  *	snake_case for: data types (except types declared in the libraries used)
  *	UPPER_CASE for: global constants, enumerations elements, macro constants
 ```Kotlin
      val PI_CONSTANT = 3.1415926;
      
      class my_class {
          var memberData : Int
    	  fun ProcessData() {
    	  }
      };
      
      fun Foo(a : Int) {
		 val maxNumber = 0
         val ballForceThrow = 0.0
      }
      
      fun OpenFile() {
      ...
      }
 ```
  4) Indentation
  *	Use only spaces, 4 spaces
  *	Any inner block must be indented
  *	Data access specifiers ("public", "protected", "private") has same indentation as class
  *	"case" markers has same indentation as "switch" block
  ```Kotlin
  class my_class {
      public fun ProcessData() {
          when (memberData) {
          0 -> memberData++
          1 -> memberData--
          2 -> printf("Error")
          }
      }
      private val memberData : Integer
  };
  ```
  5) If function fits the screen, variables declares in the beginning, else in the place of use. 
  6) Insert spaces between operators and operands.
 ```Kotlin
      val x = (a + b) * c / d + foo()
 ```
 7) Each variable declaration on a new line.
  ```Kotlin
      var x = 3
      val y = 7
      val z = 4.25
 ```
 8) When the line gets longer than 100 characters, divide it into two, making a newline after the operator, and continue writing.
 ```Kotlin
   val result = reallyLongFunctionOne() + reallyLongFunctionTwo() + 
        reallyLongFunctionThree() + reallyLongFunctionFour()
 ```
 9) Leave blank lines between functions and between expression groups.
  ```Kotlin
   fun Foo() {
    ...
   }
                          // Empty line
   fun Bar() {
    ...
   }
 ```
 10) Each function and each class must be started with comment, which will explain in simple way what this function/class do.
   ```Kotlin
   // parse string to array of tokens. Returns true in case of success and false otherwise
   fun Parser(s : String) {
    ...
   }
 ```
 