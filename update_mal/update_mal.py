# Copyright 2019 Foreseeti AB
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import os
import sys

def is_letter(s):
    return s.isalpha() or s == '$' or s == '_'

class MalUpdater():
    def __init__(self, in_filename, out_filename):
        # Input file
        if in_filename == '-':
            self.in_filename = 'stdin'
            self.input = sys.stdin.read()
        else:
            if not os.path.exists(in_filename):
                sys.exit('{}: No such file or directory'.format(in_filename))
            if not os.path.isfile(in_filename):
                sys.exit('{}: Not a file'.format(in_filename))
            self.in_filename = in_filename
            try:
                with open(in_filename) as f:
                    self.input = f.read()
            except OSError as e:
                sys.exit('{}: Could not open file for reading'.format(in_filename))

        self.in_array = []
        for c in self.input:
            self.in_array += [c]

        # Output file
        self.out_file = None
        if out_filename == '-':
            self.out_filename = 'stdout'
            self.out_file = sys.stdout
        else:
            if os.path.exists(out_filename):
                sys.exit('{}: File already exists'.format(out_filename))
            self.out_filename = out_filename
            try:
                self.out_file = open(out_filename, 'w')
            except OSError as e:
                sys.exit('{}: Could not open file for writing'.format(out_filename))

        # Positional variables
        self.pos = 0
        self.line = 1
        self.col = 1
        if self.pos < len(self.in_array):
            self.cur = self.in_array[self.pos]
        else:
            self.cur = None

        # Contextual variables
        self.in_asset = False
        self.in_attackstep = False
        self.in_ttc = False

    def __del__(self):
        if self.out_file is not None and self.out_file is not sys.stdout:
            self.out_file.close()

    def next(self, n=1):
        for i in range(n):
            if self.cur is not None:
                self.pos += 1
                if self.cur == '\n':
                    self.line += 1
                    self.col = 1
                else:
                    self.col += 1
                if self.pos < len(self.in_array):
                    self.cur = self.in_array[self.pos]
                else:
                    self.cur = None

    def peek(self, s):
        for i in range(len(s)):
            if self.pos + i >= len(self.in_array) or s[i] != self.in_array[self.pos + i]:
                return False
        return True

    def get_peek(self, n):
        if self.pos + n > len(self.in_array):
            return None
        peek = ''
        for i in range(n):
            peek += self.in_array[self.pos + i]
        return peek

    def write(self, output):
        self.out_file.write(output)

    def error(self, message):
        sys.exit("{}:{}:{}: {}".format(self.in_filename, self.line, self.col, message))

    def skip(self):
        while True:
            if self.cur is None:
                break
            elif self.peek('//'):
                self.write('//')
                self.next(2)
                while self.cur is not None and self.cur not in '\r\n':
                    self.write(self.cur)
                    self.next()
            elif self.cur in ' \t\r\n':
                self.write(self.cur)
                self.next()
            else:
                break

    def number(self):
        line = self.line
        col = self.col
        has_digit = False
        while self.cur.isdigit():
            has_digit = True
            self.write(self.cur)
            self.next()
        if self.cur == '.':
            self.write('.')
            self.next()
        while self.cur.isdigit():
            has_digit = True
            self.write(self.cur)
            self.next()
        if not has_digit:
            self.error('no digit found at {}:{}'.format(line, col))

    def update(self):
        while self.cur is not None:
            self.skip()
            if self.cur is None:
                break
            line = self.line
            col = self.col
            peek3 = self.get_peek(3)
            if peek3 is not None:
                if peek3 in ['<--', '-->']:
                    self.write(peek3)
                    self.next(3)
                    continue
                elif peek3 == '0-1':
                    # Multiplicity range has changed from '-' to '..'
                    self.write('0..1')
                    self.next(3)
                    continue
                elif peek3 == '1-*':
                    # Multiplicity range has changed from '-' to '..'
                    self.write('1..*')
                    self.next(3)
                    continue
            peek2 = self.get_peek(2)
            if peek2 is not None:
                if peek2 in ['/\\', '\\/']:
                    self.write(peek2)
                    self.next(2)
                    continue
                elif peek2 in ['->', '+>', '<-']:
                    self.write(peek2)
                    self.next(2)
                    self.in_attackstep = False
                    continue
            if self.cur in '|&#':
                self.write(self.cur)
                self.next()
                if self.in_asset:
                    self.in_attackstep = True
            elif self.peek('3'):
                # Not-exist steps have changed from '3' to '!E'
                self.write('!E')
                self.next()
                if self.in_asset:
                    self.in_attackstep = True
            elif self.peek('['):
                self.next()
                self.write('[')
                if self.in_attackstep:
                    self.in_ttc = True
            elif self.peek(']'):
                self.next()
                self.write(']')
                self.in_ttc = False
            elif self.peek('}'):
                self.next()
                self.write('}')
                self.in_asset = False
                self.in_attackstep = False
            elif self.peek('+'):
                # Transitive step has changed from '+' to '*'
                self.next()
                self.write('*')
            elif self.cur in '1*(){:,.= \t\r\n':
                self.write(self.cur)
                self.next()
            elif is_letter(self.cur):
                identifier = self.cur
                self.next()
                while is_letter(self.cur) or self.cur.isdigit():
                    identifier += self.cur
                    self.next()
                if identifier == 'abstractAsset':
                    # Keyword 'abstractAsset' has changed to 'abstract asset'
                    self.write('abstract asset')
                    self.in_asset = True
                elif identifier == 'asset':
                    self.write('asset')
                    self.in_asset = True
                elif identifier == 'E':
                    self.write('E')
                    if self.in_asset:
                        self.in_attackstep = True
                elif identifier == 'info' or identifier == 'rationale' or identifier == 'assumptions':
                    self.write(identifier)
                    self.in_attackstep = False
                elif identifier == 'include':
                    self.write('include')
                    self.skip()
                    include = ''
                    trailing = ''
                    while True:
                        if self.cur in ' \t':
                            trailing += self.cur
                            self.next()
                        elif self.cur in './\\':
                            include += trailing
                            include += self.cur
                            trailing = ''
                            self.next()
                        elif is_letter(self.cur):
                            include += trailing
                            include += self.cur
                            trailing = ''
                            self.next()
                            while is_letter(self.cur) or self.cur.isdigit():
                                include += self.cur
                                self.next()
                        else:
                            break
                    if include == '':
                        self.error('empty include')
                    self.write('"')
                    self.write(include)
                    self.write('"')
                    self.write(trailing)
                elif self.in_ttc:
                    if identifier.lower().endswith('distribution'):
                        self.write(identifier[:-len('distribution')])
                    else:
                        self.write(identifier)
                    self.skip()
                    if self.cur == '(':
                        self.write('(')
                        self.next()
                        self.skip()
                        self.number()
                        self.skip()
                        while self.cur == ',':
                            self.write(',')
                            self.next()
                            self.skip()
                            self.number()
                            self.skip()
                        if self.cur != ')':
                            self.error("expected ')'")
                else:
                    self.write(identifier)
            elif self.peek('"'):
                string = ''
                self.next()
                while self.cur is not None and self.cur != '"' and self.cur != '\\':
                    string += self.cur
                    self.next()
                if self.cur != '"':
                    self.error('unterminated string starting at {}:{}'.format(line, col))
                self.next()
                self.write('"')
                for ch in string:
                    if ch == '\n':
                        self.write('\\n')
                    elif ch == '\r':
                        self.write('\\r')
                    elif ch == '\t':
                        self.write('\\t')
                    else:
                        self.write(ch)
                self.write('"')
            elif self.peek('//'):
                self.next(2)
                self.write('//')
                while self.cur is not None and self.cur != '\r' and self.cur != '\n':
                    self.write(self.cur)
                    self.next()
            else:
                self.error("invalid token '{}'".format(self.cur))

def main():
    parser = argparse.ArgumentParser(description='Updates old MAL specifications to the current format')
    parser.add_argument('-i', '--input', required=True, help='Input file', metavar='INPUT')
    parser.add_argument('-o', '--output', required=True, help='Output file', metavar='OUTPUT')
    args = parser.parse_args()
    updater = MalUpdater(args.input, args.output)
    updater.update()
    print("Output written to", updater.out_filename, file=sys.stderr)

if __name__ == '__main__':
    main()
