IGB Compiler L1 compiles **IGB L1 code** into **IGB Binary**.  
You may call it an *assembly language*.  


The [IGB CL2](https://github.com/krypciak/IGB-Compiler-L2) compiles high-level code into this low-level code.  

The instruction set file can be seen [here](/resources/commandSet.txt).
## Commant set symbol explanation:
- `*string*|*number*` If the current argument equals *string*, *number* is returned.
- `i` The input value is returned as it is.
- `d` The input value gets multiplied by 1000  (learn why [here](https://github.com/krypciak/IGB-VM/edit/main/README.md#floating-points-values))
- `@` accepts two arguments:  
  - `n` that returns 0  
  - `c` that returns 1  
If it's next argument is 'd' and the current argument equals 'c', the next argument isn't multiplied by 1000.
- `P` If the argument is a string, returns the cell of a pointer. If the argument is an integer, returns it.

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
<br /><br />

## Memory cells  
There are cells allocated for the [IGB VM](https://github.com/krypciak/IGB-VM).
You are safe to use cells 70 or above.
Here are some more important cells:  
&emsp; 0 - Function return  
&emsp; 1 - Screen width  
&emsp; 2 - Screen height  
&emsp; 3 - Screen type (`0=16c`, `1=rgb`)  
&emsp; 4 - Keyboard input char  
&emsp; 56 - [charLib](https://github.com/krypciak/IGB-charLib) char code  
&emsp; 57 - [charLib](https://github.com/krypciak/IGB-charLib) char x  
&emsp; 58 - [charLib](https://github.com/krypciak/IGB-charLib) char y  

The full list can be seen [here](/src/me/krypek/igb/cl1/IGB_MA.java).
<br /><br />

## Instruction explanation:

### 0. If
&emsp; Syntax:  `If *operation* i @ d P`  
&emsp; If cell `arg2` *operation* `arg4 / |arg4|` is false then jump to `arg5` (pointer or integer)  
<br />
&emsp; Example: `If > 70 n 5 :myfunction`  
&emsp; Which means: `If cell 70 is NOT higher than the number 5000 jump to :mypointer`

### 1. Init
&emsp; Syntax: `Init d i`  
&emsp; Writes `arg1` to cell `arg2`  
<br />
&emsp; Example: `Init 5.3 70`  
&emsp; Which means: `Write 5300 to cell 70`

### 2. Copy
&emsp; Syntax: `Copy i i`  
&emsp; Copies cell `arg1` to cell `arg2`  
<br />
&emsp; Example: `Copy 70 71`  
&emsp; Which means: `Read cell 70 and write it to cell 71`

### 3. Add
&emsp; Syntax: `Add i @ d i`  
&emsp; Adds cell `arg1` to `arg3 / |arg3|` and store the result in cell `arg4`  
&emsp; Why a seperate instruction for adding numbers? It's faster that way.  
<br />
&emsp; Example: `Add 70 c 71 72`  
&emsp; Which means: `Add cell 70 and cell 71 and store the result in cell 72`  

### 4. Cell
&emsp; Jump Syntax: `Cell Jump P`  
&emsp; Sets the current cell to `arg2`  
<br />
&emsp; Call Syntax: `Cell Call P`  
&emsp; Stores the current cell on a stack and sets the current cell to `arg2`  
<br />
&emsp; Return Syntax: `Cell Return`  
&emsp; Pops the stack and sets the current cell to it

### 5. Pixel
&emsp; The screen type (`cell 3`) determinates which syntax will do what at runtime.  
&emsp; I recommend that you read about more about pixel cache [here](https://github.com/krypciak/IGB-VM/blob/main/README.md#pixel-cache).  
<br />
- `Pixel @ i @ i`  
&emsp; Sets the pixel at x=`arg2 / |arg2|`, y=`arg4 / |arg4|` with the color stored in the pixel cache.

- `Pixel Cache Raw i`  
&emsp; Sets the pixel cache to `arg3`  
<br />
&emsp; RGB exclusive synax:

- `Pixel Cache @ i @ i @ i`  
&emsp; Calculated at runtime the pixel cache from the arguments. (`arg3` is r, `arg5` is g, `arg7` is b)  
&emsp; If all arguments are `d`, the cache is computed at compile-time and the instruction is swapped with `Pixel Cache Raw`.  

- `Pixel @ i @ i i`  
&emsp; Gets the rgb color from pixel at x=`arg2 / |arg2|`, y=`arg4 / |arg4|` and
  - r is writen to cell `arg5`
  - g is written to cell `arg5`+1
  - b is written to cell `arg5`+2
<br />
&emsp; 16c exclusive synax:<br />

- `Pixel Cache i`  
&emsp; Sets pixel cache to cell `arg2`  

- `Pixel @ i @ i i`  
&emsp; Gets the 16c color from pixel at x=`arg2 / |arg2|`, y=`arg4 / |arg4|` and writes it to `arg5`  

### 6. Device  
- `Device CoreWait i`  
&emsp; Waits `arg2` ticks (a tick is 1/20 of a second)  

- `Device ScreenUpdate`  
&emsp; Resizes the screen based on the [cells in memory](https://github.com/krypciak/IGB-Compiler-L1/edit/main/README.md#memory-cells) and filles it with #FFFFFF  

- `Device Log @ d`  
&emsp; Prints `arg3 / |arg3|` to the terminal/chat.  

### 7. Math  

- Syntax for `-` `*` `/` `%` operations: `Math *operation* i @ d i`  
&emsp; It does what you think.  

- Syntax for random: `Math R i i i`  
&emsp; Writes a random number from `arg2` to `arg3` to cell `arg4`  
<br />

- Syntax for `CC`: `Math CC i i`  
&emsp; It reads the value from cell `arg2`, then reads the value at cell it just read, then writes in to cell `arg3`  
&emsp; If you assume ram is an array, then that's how it works:  
&emsp; `ram[ arg3 ] = ram[ ram[ arg2 ] ]`  
&emsp; Used for reading from arrays.  
<br />

- Syntax for `CW`: `Math CW i i`  
&emsp; Writes value from cell `arg2` to cell read from cell `arg3`  
&emsp; If you assume ram is an array, then that's how it works:  
&emsp; `ram[ ram[ arg3 ] ] = ram[ arg2 ]`  
&emsp; Used for writing to arrays.  

- Synax for sqrt: `Math sqrt i i`  
Writes the square root of cell `arg2` to cell `arg3`  


<br /><br /><br />

# License
Licensed under GNU GPLv3 or later

