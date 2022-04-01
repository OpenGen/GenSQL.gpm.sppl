{ pkgs ? import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/35a74aa665a681b60d68e3d3400fcaa11690ee50.tar.gz") {} }:

let
  pypkgs = pkgs.python39Packages;

  sppl = pypkgs.buildPythonPackage rec { # not in nixpkgs
    pname = "sppl";
    version = "1.2.1";

    src = pypkgs.fetchPypi {
      inherit pname version;
      sha256 = "sha256-x47vrQeu96uCt+3ZfsVy80gTevOAILJIL2umEdkMCkM=";
    };

    propagatedBuildInputs = with pypkgs; [
      astunparse
      numpy
      scipy
      sympy
    ];

    checkInputs = with pypkgs; [
      coverage
      pytest
      pytestCheckHook
      pytest-timeout
    ];

    pytestFlagsArray = [ "--pyargs" "sppl" ];

    pipInstallFlags = [ "--no-deps" ];
  };

  python = pkgs.python39.withPackages (p: [ sppl ]);
in pkgs.mkShell {
  buildInputs = [
    pkgs.clojure
    pkgs.openjdk11
    python
  ];
  shellHook = "export PYTHONPATH=${python}/${python.sitePackages}";
}
