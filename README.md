IGB Compiler L1 compiles **IGB L1 code** into **IGB Binary**.  
You may call it an *assembly language*.  


The [IGB CL2](https://github.com/krypciak/IGB-Compiler-L2) compiles high-level code into this low-level code.  

The instruction set file can be seen [here](/resources/commandSet.txt).
## Commant set symbol explanation:
- *string*|*number* &emsp; If the current argument equals *string*, *number* is returned.
- 'i' &emsp; The input value is returned as it is.
- 'd' &emsp; The input value gets multiplied by 1000
- '@' &emsp; accepts two arguments:  
  - 'n' that returns 0  
  - 'c' that returns 1  
If it's next argument is 'd' and the current argument equals 'c', the next argument isn't multiplied by 1000.
- 'P' &emsp; If the argument is a string, returns the cell of a pointer. If the argument is an integer, returns it.

#### Example:  
 
Hint: |12| means cell 12

input: `Math % 10 c 11 12`  
syntax match: `Math|7 %|3 i @ d i`  
output: `7 1 10 1 11 12`
 
That instruction can be translated into `|12| = |10| % |12|`
<br /><br />
##### If you swap the 'c' for a 'n', the second last argument will be 1000 times bigger.
input: `Math % 10 n 11 12`  
syntax match: `Math|7 %|3 i @ d i`  
output: `7 1 10 0 11000 12`
 
That instruction can be translated into `|12| = |10| % 12`
<br /><br /><br /><br />
## Instruction explanation:
### 0. If
 
### 2. Init
