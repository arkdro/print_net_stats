# net_stats

Process the log create by `net-stats.sh` (which uses `ip --json .....`) and print
the counters as well as the delta.

## Usage

`java -jar net_stats.jar -f net-stats.log -m whole_file -i wlp3s0`

or

`java -jar net_stats.jar -f net-stats.log -m file_by_lines -i wlp3s0`

## License

Copyright © 2022 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
